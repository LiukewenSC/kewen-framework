package com.kewen.framework.common.core.handler;

import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @descrpition web拦截器
 * @author kewen
 * @since 2022-12-05 14:19
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

    /**
     * 业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> bizException(BizException e){
        log.error("bizException >> ",e);
        return Result.failed(Result.BIZ_FAILED,e.getMessage());
    }
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> bizException(NullPointerException e){
        log.error("nullPointerException >> ",e);
        return Result.failed(Result.BIZ_FAILED,e.getMessage());
    }


    /**
     * 参数校验
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e){

        BindingResult bindingResult = e.getBindingResult();

        StringBuilder builder = new StringBuilder();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            builder.append("[").append(fieldName).append("]").append(message).append(";");
        }
        //去掉最后一个;
        String message = builder.substring(0, builder.length() - 2);


        return Result.failed(Result.REQUEST_REJECT,message);
    }

    /**
     * 全局异常
     * @param e
     * @return
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus
    public Result<Void> throwable(Throwable e){
        log.error("throwable >> ",e);
        return Result.failed(e.getMessage());
    }

}
