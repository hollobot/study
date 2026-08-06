package com.hello.springevent.common;

/**
 * 统一接口响应体。
 *
 * @param code 业务响应码，0 表示成功
 * @param message 响应说明
 * @param data 响应数据
 */
public record ApiResponse<T>(int code, String message, T data) {

    /**
     * 构造成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 统一成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 构造失败响应。
     *
     * @param message 失败原因
     * @return 统一失败响应
     */
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(400, message, null);
    }
}
