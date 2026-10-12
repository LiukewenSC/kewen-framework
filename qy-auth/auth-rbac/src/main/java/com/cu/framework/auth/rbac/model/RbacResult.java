package com.cu.framework.auth.rbac.model;

import lombok.Data;

/**
 * @author kewen
 * @since 2024-05-11
 */
@Data
public class RbacResult<T> {
    private Integer code = 200;
    private Boolean success = true;
    private String message = "成功";
    private T data;
    public static <T> RbacResult<T> success(){
        return success(null);
    }
    public static <T> RbacResult<T> success(T data){
        RbacResult<T> rbacResult = new RbacResult<T>();
        rbacResult.setData(data);
        return rbacResult;
    }
    public static RbacResult failed(String message){
        RbacResult rbacResult = new RbacResult();
        rbacResult.setSuccess(false);
        rbacResult.setCode(500);
        rbacResult.setMessage(message);
        return rbacResult;
    }
    public static RbacResult failed(int code , String message){
        RbacResult rbacResult = new RbacResult();
        rbacResult.setSuccess(false);
        rbacResult.setCode(code);
        rbacResult.setMessage(message);
        return rbacResult;
    }

}
