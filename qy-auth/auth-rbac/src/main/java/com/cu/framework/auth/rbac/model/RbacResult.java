package com.cu.framework.auth.rbac.model;

import lombok.Data;

/**
 * @author kewen
 * @since 2024-05-11
 */
@Data
public class RbacResult<T> {
    /** 状态码，成功默认 200，failed 默认置为 500 */
    private Integer code = 200;
    /** 是否成功，默认 true，failed 时置为 false */
    private Boolean success = true;
    /** 提示信息，默认"成功" */
    private String message = "成功";
    /** 响应数据 */
    private T data;
    /**
     * 返回不带数据的成功结果
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> RbacResult<T> success(){
        return success(null);
    }
    /**
     * 返回携带数据的成功结果
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> RbacResult<T> success(T data){
        RbacResult<T> rbacResult = new RbacResult<T>();
        rbacResult.setData(data);
        return rbacResult;
    }
    /**
     * 返回失败结果，状态码为 500
     * @param message 失败提示信息
     * @return 失败结果
     */
    public static RbacResult failed(String message){
        RbacResult rbacResult = new RbacResult();
        rbacResult.setSuccess(false);
        rbacResult.setCode(500);
        rbacResult.setMessage(message);
        return rbacResult;
    }
    /**
     * 返回指定状态码的失败结果
     * @param code 状态码
     * @param message 失败提示信息
     * @return 失败结果
     */
    public static RbacResult failed(int code , String message){
        RbacResult rbacResult = new RbacResult();
        rbacResult.setSuccess(false);
        rbacResult.setCode(code);
        rbacResult.setMessage(message);
        return rbacResult;
    }

}
