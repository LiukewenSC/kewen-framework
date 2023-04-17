package com.kewen.framework.cloud.alibaba.nacos.client.exception;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.kewen.framework.common.core.model.Result;
import com.netflix.client.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class NacosClientExceptionAdvance {

    @ExceptionHandler(ClientException.class)
    public Result clientException(ClientException ex){

        log.error("feignClientException >> ",ex);
        return Result.failed("feign调用异常： "+ex.getMessage());
    }

    @ExceptionHandler(FlowException.class)
    public Result flowException(FlowException ex){

        log.error("flowException >> ",ex);
        return Result.failed("sentinel限流： "+ex.getMessage());
    }
    @ExceptionHandler(DegradeException.class)
    public Result degradeException(DegradeException ex){

        log.error("degradeException >> ",ex);
        return Result.failed("sentinel 降级： "+ex.getMessage());
    }

}
