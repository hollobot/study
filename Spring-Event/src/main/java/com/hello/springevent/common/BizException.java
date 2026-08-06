package com.hello.springevent.common;

public class BizException extends RuntimeException {

    /**
     * 创建业务异常。
     *
     * @param message 面向接口调用方的错误说明
     */
    public BizException(String message) {
        super(message);
    }
}
