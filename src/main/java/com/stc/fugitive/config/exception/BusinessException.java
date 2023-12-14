package com.stc.fugitive.config.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 8608742776449692876L;

    private Integer code;
    private Object[] args;

    public BusinessException(String msg) {
        super(msg);
    }

    public BusinessException(Integer code, String msg, Object... args) {
        super(msg);
        this.code = code;
        this.args = args;
    }

    public BusinessException(Integer code, String msg, Throwable cause, Object... args) {
        super(msg, cause);
        this.code = code;
        this.args = args;
    }
}
