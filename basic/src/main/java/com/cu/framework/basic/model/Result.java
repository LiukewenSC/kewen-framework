package com.cu.framework.basic.model;


import lombok.Data;

/**
 * @descrpition 返回类
 * @author kewen
 * @since 2022-11-25 13:48
 */
@Data
public class Result<T> {

    /** 响应码，成功固定为 200，失败由调用方指定或默认 500 */
    protected Integer code;
    /** 是否成功，success 工厂方法为 true，failed 工厂方法为 false */
    protected Boolean success ;
    /** 提示信息，成功固定为"成功"，失败为具体错误信息 */
    protected String message;
    /** 业务数据 */
    protected T data;

    /**
     * 构建不带数据的成功返回，code=200，message="成功"
     * @return 成功的 Result
     */
    public static Result<Void> success(){
        Result<Void> result = new Result<>();
        result.code=200;
        result.success=true;
        result.message="成功";
        return result;
    }
    /**
     * 构建携带数据的成功返回，code=200，message="成功"
     * @param data 业务数据
     * @param <T> 业务数据类型
     * @return 成功的 Result
     */
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.code=200;
        result.message="成功";
        result.success=true;
        result.data=data;
        return result;
    }
    /**
     * 构建指定错误码的失败返回，success=false
     * @param code 错误码
     * @param message 错误信息
     * @return 失败的 Result
     */
    public static Result<Void> failed(Integer code,String message){
        Result<Void> result = new Result<>();
        result.code=code;
        result.success=false;
        result.message=message;
        return result;
    }
    /**
     * 构建失败返回，错误码默认 500，success=false
     * @param message 错误信息
     * @return 失败的 Result
     */
    public static Result<Void> failed(String message){
        Result<Void> result = new Result<>();
        result.code=500;
        result.success=false;
        result.message=message;
        return result;
    }
}
