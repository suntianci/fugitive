package com.stc.fugitive.config.exception;

import com.stc.fugitive.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.util.UUID;

@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class) //处理业务异常
    public Result handlException(BusinessException e) {
        String uid = "[" + getUuid() + "]";
        log.error("业务异常处理! uid:{}", uid, e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class) //处理统一异常
    public Result handlException(Exception e) {
        String uid = "[" + getUuid() + "]";
        log.error("业务异常处理! uid:{}", uid, e);
        if (e instanceof DuplicateKeyException) {
            return Result.fail(51003, "主键重复");
        }
        if (e instanceof DataAccessException) {
            return Result.fail(51002, "数据库访问异常");
        }
        if (e instanceof SQLException) {
            return Result.fail(51001, "数据库异常");
        }

        return Result.fail(50001, e.getMessage());
    }

    private String getUuid() {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString().replaceAll("-", "");
        return id + String.valueOf(System.currentTimeMillis());
    }


}
