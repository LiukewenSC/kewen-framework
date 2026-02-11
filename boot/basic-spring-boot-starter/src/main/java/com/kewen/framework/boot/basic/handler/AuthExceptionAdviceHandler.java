package com.kewen.framework.boot.basic.handler;


import com.kewen.framework.basic.exception.ExceptionConstant;
import com.kewen.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionAdviceHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthExceptionAdviceHandler.class);


    @ExceptionHandler(Throwable.class)
    public Result throwException(Throwable t){
        logger.error("全局异常：{}", t.getMessage(), t);
        return Result.failed(ExceptionConstant.BIZ_FAILED, t.getMessage());
    }
}
