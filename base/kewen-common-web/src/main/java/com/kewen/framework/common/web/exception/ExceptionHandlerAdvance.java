package com.kewen.framework.common.web.exception;

import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @descrpition web拦截器
 * @author kewen
 * @since 2022-12-05 14:19
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerAdvance {

    @ExceptionHandler(BizException.class)
    @ResponseStatus
    public Result<Void> bizException(BizException e){
        log.error("bizException >> ",e);
        return Result.failed(e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public Result<Void> exception(Exception e){
        log.error("exception >> ",e);
        return Result.failed(e.getMessage());
    }
    @ExceptionHandler(Throwable.class)
    @ResponseStatus
    public Result<Void> throwable(Throwable e){
        log.error("throwable >> ",e);
        return Result.failed(e.getMessage());
    }

}
