package com.kewen.framework.base.common.model;


import lombok.Data;
import lombok.Getter;

/**
 * @descrpition 返回类
 * @author kewen
 * @since 2022-11-25 13:48
 */
@Getter
public class Result<T> {

    public static final Integer LOGIN_FAILED=10001;
    public static final Integer NO_LOGIN=401;

    private Integer code;
    private Boolean success ;
    private String message;
    private T data;

    private Result() {
    }

    public static Result<Void> success(){
        Result<Void> result = new Result<>();
        result.code=200;
        result.success=true;
        result.message="成功";
        return result;
    }
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.code=200;
        result.message="成功";
        result.success=true;
        result.data=data;
        return result;
    }
    public static Result<Void> failed(Integer code,String message){
        Result<Void> result = new Result<>();
        result.code=code;
        result.success=false;
        result.message=message;
        return result;
    }
    public static Result<Void> failed(String message){
        Result<Void> result = new Result<>();
        result.code=500;
        result.success=false;
        result.message=message;
        return result;
    }
}
