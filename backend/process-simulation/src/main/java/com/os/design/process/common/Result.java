package com.os.design.process.common;

import lombok.Data;

/**
 * 统一 API 响应结果封装
 * 所有的 Controller 接口都必须返回这个对象
 */
@Data
public class Result<T> {
    private int code;       // 状态码：200成功，500失败
    private String msg;     // 提示信息
    private T data;         // 返回的数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        return result;
    }
}