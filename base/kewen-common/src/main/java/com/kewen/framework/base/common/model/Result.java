package com.kewen.framework.base.common.model;



/**
 * @descrpition 返回类
 * @author kewen
 * @since 2022-11-25 13:48
 */
public class Result<T> {
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
    public static Result<Void> failed(String message){
        Result<Void> result = new Result<>();
        result.code=500;
        result.success=false;
        result.message=message;
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
