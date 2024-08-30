package com.kewen.framework.basic.logger.trace;


import com.kewen.framework.basic.model.Result;
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

    private String traceId;

    public static <T> TraceResult<T> of(Result<T> result,String traceId) {
        //坑爹的fastjson
        TraceResult<T> traceResult = new TraceResult<>();
        traceResult.setCode(result.getCode());
        traceResult.setMessage(result.getMessage());
        traceResult.setSuccess(result.getSuccess());
        traceResult.setData(result.getData());
        traceResult.setTraceId(traceId);
        return traceResult;
    }
}
