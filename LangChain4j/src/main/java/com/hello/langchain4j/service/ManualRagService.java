package com.hello.langchain4j.service;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualRagService {

    /** 向量库数据库检索内容服务 */
    private final ContentRetriever contentRetriever;

    /** 流式输出模型对话服务 */
    private final StreamingChatService streamingChatService;

    /**
     * RAG对话服务
     *
     * @param question 用户问题
     * @return
     */
    public Flux<String> chat(String question) {

        // 向量数据检索相关知识
        Query query = Query.from(question);
        List<Content> relevantContents = contentRetriever.retrieve(query);
        System.out.println("候选人问题:" + question);
        System.out.println("知识库检索:");
        for (int i = 0; i < relevantContents.size(); i++) {
            System.out.println((i + 1) + "." + relevantContents.get(i).textSegment().text());
        }

        // 构建向量数据库检索的知识提示词
        String context;
        if (relevantContents.isEmpty()) {
            context = "（知识库中未找到相关内容）";
        } else {
            context = relevantContents.stream().map(c -> c.textSegment().text()).collect(Collectors.joining("\n\n---\n\n")); // 用分隔符隔开每个片段
        }

        // 系统提示词
        SystemMessage systemMessage = SystemMessage.from("""
            你是专属招聘咨询助手，只负责回复候选人关于薪资、绩效、底薪、发放、社保、提成、试用期等岗位相关问题。
            规则：
            1. 只使用提供的【知识点】内容回答，**绝不编造、绝不扩展、绝不脑补**。
            2. 回答口语化、友好、简洁，符合HR正常回复语气。
            3. 没有匹配到知识点时，统一回复：「这个问题我暂时无法准确回答，建议你咨询面试组长。」
            4. 多条知识点相关时，按逻辑合并，不重复、不啰嗦。
            5. 严格忠于知识库，不添加任何知识库以外的信息。
            """);

        // 构建用户提示词
        String userMessageText = String.format("""
            【知识点】
            %s
            
            【问题】
            %s
            """, context, question);
        UserMessage userMessage = UserMessage.from(userMessageText);

        // 3. 流式调用
        return streamingChatService.streamWithOllama(systemMessage, userMessage);
    }
}