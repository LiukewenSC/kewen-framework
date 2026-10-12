package com.cu.framework.boot.basic.handler;


import com.cu.framework.basic.exception.ExceptionConstant;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，捕获所有未处理异常并统一包装为失败的 Result 返回
 * @author kewen
 * @since 2024-08-29
 */
@RestControllerAdvice
public class AuthExceptionAdviceHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthExceptionAdviceHandler.class);


    /**
     * 处理任意 Throwable，记录错误日志后返回 BIZ_FAILED 错误码及异常信息
     * @param t 捕获到的异常
     * @return 携带异常信息的失败结果
     */
    @ExceptionHandler(Throwable.class)
    public Result throwException(Throwable t){
        logger.error("全局异常：{}", t.getMessage(), t);
        return Result.failed(ExceptionConstant.BIZ_FAILED, t.getMessage());
    }
}
