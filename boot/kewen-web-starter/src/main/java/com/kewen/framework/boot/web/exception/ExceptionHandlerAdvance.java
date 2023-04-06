package com.kewen.framework.boot.web.exception;

import com.kewen.framework.base.common.exception.BizException;
import com.kewen.framework.base.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public Result<Void> bizException(BizException e){
        log.error("bizException >> ",e);
        return Result.failed(e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result<Void> exception(Exception e){
        log.error("exception >> ",e);
        return Result.failed(e.getMessage());
    }
    @ExceptionHandler(Throwable.class)
    public Result<Void> throwable(Throwable e){
        log.error("throwable >> ",e);
        return Result.failed(e.getMessage());
    }

}
