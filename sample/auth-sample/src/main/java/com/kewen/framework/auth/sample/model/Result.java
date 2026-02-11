package com.kewen.framework.auth.sample.model;


import lombok.Data;

/**
 * 
 * @author kewen
 * @since 2024-08-30
 */
@Data
public class Result<T> {
    private Integer code = 200;
    private Boolean success = true;
    private String message = "成功";
    private T data;
    public static <T> Result<T> success(){
        return success(null);
    }
    public static <T> Result<T> success(T data){
        Result<T> rabcResult = new Result<T>();
        rabcResult.setData(data);
        return rabcResult;
    }
    public static Result failed(String message){
        Result rabcResult = new Result();
        rabcResult.setSuccess(false);
        rabcResult.setCode(500);
        rabcResult.setMessage(message);
        return rabcResult;
    }
    public static Result failed(int code , String message){
        Result rabcResult = new Result();
        rabcResult.setSuccess(false);
        rabcResult.setCode(code);
        rabcResult.setMessage(message);
        return rabcResult;
    }

}
