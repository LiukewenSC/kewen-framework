package com.cu.framework.basic.exception;


import com.cu.framework.basic.model.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @descrpition web拦截器
 * @author kewen
 * @since 2022-12-05 14:19
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandlerAdvice {

    private static final String DATABASE_FAILED_MESSAGE = "数据库操作失败，请稍后重试";
    private static final String SYSTEM_FAILED_MESSAGE = "系统内部错误，请稍后重试";
    private static final String VALIDATION_FAILED_MESSAGE = "参数校验失败";

    /**
     * 业务异常
     * @param e 业务异常
     * @param request 当前请求
     * @return code 400 的业务失败响应
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> bizException(BizException e, HttpServletRequest request){
        ErrorLog.warn(ErrorType.BUSINESS, request, e.getMessage());
        return Result.failed(ErrorType.BUSINESS, e.getMessage());
    }

    /**
     * 参数校验与表单绑定失败
     * @param e 绑定异常
     * @param request 当前请求
     * @return code 400 的校验失败响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> bindException(BindException e, HttpServletRequest request){
        String message = validationMessage(e);
        ErrorLog.warn(ErrorType.VALIDATION, request, message);
        return Result.failed(ErrorType.VALIDATION, message);
    }

    /**
     * 请求体无法解析
     * @param e 请求体解析异常
     * @param request 当前请求
     * @return code 400 的校验失败响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request){
        String message = "请求体格式不正确";
        ErrorLog.warn(ErrorType.VALIDATION, request, message + "：" + e.getMessage());
        return Result.failed(ErrorType.VALIDATION, message);
    }

    /**
     * 参数类型转换失败
     * @param e 类型不匹配异常
     * @param request 当前请求
     * @return code 400 的校验失败响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request){
        String message = "参数[" + e.getName() + "]格式不正确";
        ErrorLog.warn(ErrorType.VALIDATION, request, message);
        return Result.failed(ErrorType.VALIDATION, message);
    }

    /**
     * 缺少必需请求参数
     * @param e 缺少参数异常
     * @param request 当前请求
     * @return code 400 的校验失败响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> missingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request){
        String message = "缺少必需参数[" + e.getParameterName() + "]";
        ErrorLog.warn(ErrorType.VALIDATION, request, message);
        return Result.failed(ErrorType.VALIDATION, message);
    }

    /**
     * 数据库访问失败，只向调用方返回通用提示
     * @param e 数据库访问异常
     * @param request 当前请求
     * @return code 500 的数据库失败响应
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> dataAccessException(DataAccessException e, HttpServletRequest request){
        ErrorLog.error(ErrorType.DATABASE, request, "数据库操作失败", e);
        return Result.failed(ErrorType.DATABASE, DATABASE_FAILED_MESSAGE);
    }

    /**
     * 全局异常，已包装的数据库异常仍归入数据库分类
     * @param e 未预期异常
     * @param request 当前请求
     * @return code 500 的系统失败响应
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> throwable(Throwable e, HttpServletRequest request){
        ErrorType errorType = ErrorType.causedByDatabase(e) ? ErrorType.DATABASE : ErrorType.SYSTEM;
        String message = errorType == ErrorType.DATABASE ? DATABASE_FAILED_MESSAGE : SYSTEM_FAILED_MESSAGE;
        ErrorLog.error(errorType, request, "未预期异常：" + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        return Result.failed(errorType, message);
    }

    /**
     * 将字段校验错误拼接为 [字段名]提示;... 的形式，保留完整提示文本
     */
    private String validationMessage(BindException e){
        List<String> messages = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            messages.add("[" + fieldError.getField() + "]" + fieldError.getDefaultMessage());
        }
        return messages.isEmpty() ? VALIDATION_FAILED_MESSAGE : String.join(";", messages);
    }

}
