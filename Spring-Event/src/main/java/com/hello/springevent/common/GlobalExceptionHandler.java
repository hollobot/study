package com.hello.springevent.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理主动抛出的业务异常。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    /**
     * 处理唯一键冲突等重复请求异常。
     *
     * @param exception 数据库唯一键冲突异常
     * @return 统一失败响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKeyException(DuplicateKeyException exception) {
        return ApiResponse.fail("重复请求，请稍后刷新结果");
    }
}
