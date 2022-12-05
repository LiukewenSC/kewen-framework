package com.kewen.framework.boot.web.exception;

import com.kewen.common.exception.BizException;
import com.kewen.common.model.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @descrpition web拦截器
 * @author kewen
 * @since 2022-12-05 14:19
 */
@RestControllerAdvice
public class ExceptionHandlerAdvance {

    @ExceptionHandler(BizException.class)
    public Result<Void> bizException(BizException e){
        return Result.failed(e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result<Void> exception(Exception e){
        return Result.failed(e.getMessage());
    }
    @ExceptionHandler(Throwable.class)
    public Result<Void> throwable(Throwable e){
        return Result.failed(e.getMessage());
    }

}
