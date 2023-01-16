package com.kewen.framework.cloud.alibaba.nacos.client.exception;

import com.kewen.framework.base.common.model.Result;
import com.netflix.client.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class FeignExceptionAdvance {

    @ExceptionHandler(ClientException.class)
    public Result clientException(ClientException ex){

        log.error("feignClientException >> ",ex);
        return Result.failed("feign调用异常： "+ex.getMessage());
    }

}
