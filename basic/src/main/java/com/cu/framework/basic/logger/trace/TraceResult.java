package com.cu.framework.basic.logger.trace;


import com.cu.framework.basic.model.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *  带交易流水号的返回结构
 * @author kewen
 * @since 2024-08-30
 */
@Setter
@Getter
public class TraceResult<T> extends Result<T> {

    /** 交易流水号 */
    private String traceId;

    /**
     * 将原始 Result 的 code、message、success、data 复制到新的 TraceResult，并附带流水号
     * @param result 原始返回结果
     * @param traceId 交易流水号
     * @param <T> 业务数据类型
     * @return 携带流水号的新返回结构
     */
    public static <T> TraceResult<T> of(Result<T> result,String traceId) {
        //坑爹的fastjson
        TraceResult<T> traceResult = new TraceResult<>();
        traceResult.setCode(result.getCode());
        traceResult.setMessage(result.getMessage());
        traceResult.setSuccess(result.getSuccess());
        traceResult.setData(result.getData());
        traceResult.setErrorType(result.getErrorType());
        traceResult.setTraceId(traceId);
        return traceResult;
    }
}
