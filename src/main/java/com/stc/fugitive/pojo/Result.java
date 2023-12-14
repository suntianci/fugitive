package com.stc.fugitive.pojo;


import com.stc.fugitive.config.exception.ResultException;
import com.stc.fugitive.util.SpringContextUtil;
import lombok.Data;

/**
 * Created by liyu on 2018/1/19.
 */
@Data
public class Result<T> {
    public final static int OK = 200;

    private Integer status;
    private String msg;
    private T data;
    private String serviceName;
    private Object[] args;

    {
        this.serviceName = SpringContextUtil.getServiceName();
    }

    public Result() {
    }

    public Result(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Result(Integer status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(OK, null, data);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        return fail(code, msg, null);
    }

    public static <T> Result<T> fail(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public Result<T> validate() {
        if (!Integer.valueOf(Result.OK).equals(this.status)) {
            throw new ResultException(this);
        }
        return this;
    }


    public Result<T> withStatus(Integer code) {
        this.status = code;
        return this;
    }

    public Result<T> withMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Result<T> withData(T data) {
        this.data = data;
        return this;
    }

    public Result<T> withArgs(Object[] args) {
        this.args = args;
        return this;
    }
}
