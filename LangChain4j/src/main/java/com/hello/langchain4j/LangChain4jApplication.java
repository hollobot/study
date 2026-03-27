package com.hello.langchain4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class LangChain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangChain4jApplication.class, args);
        log.info("http://127.0.0.1:8080/swagger-ui/index.html");
        log.info("http://127.0.0.1:8080/doc.html");
    }

}
