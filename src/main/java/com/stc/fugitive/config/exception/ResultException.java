package com.stc.fugitive.config.exception;


import com.stc.fugitive.pojo.Result;

/**
 * Created by liyu on 2018/1/25.
 * 定义为直接将错误信息返回的异常
 */
public class ResultException extends RuntimeException {
    private Result result;

    public ResultException(Result result) {
        this(result, null);
    }

    public ResultException(Result result, Throwable cause) {
        super(result.getMsg(), cause);
        this.result = result;
    }

    public Result getResult() {
        return result;
    }
}
