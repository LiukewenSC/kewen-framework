package com.cu.framework.basic.logger.request;

import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 请求包装类，为了能够多读取一次数据流
 *  当不支持body参数解析的时候返回的body数组为空
 * @author liukewen
 * @since 2022/12/29
 */
public class BodyHttpServletRequest extends HttpServletRequestWrapper {

    /** 是否支持解析请求体，仅当 Content-Type 以 application/json 开头时为 true */
    private final boolean isSupportParseBody;


    /** 基于缓存 body 字节数组重建的输入流，仅支持解析 body 时初始化 */
    private ServletInputStream inputStream;
    /** 字符流读取器，对应的 getReader() 重写已被注释，当前未使用 */
    private BufferedReader reader;
    /** 缓存的请求体字节数组，不支持解析 body 时为 null */
    private byte[] body;

    /**
     * 包装原始请求；若 Content-Type 为 application/json，则将请求体一次性读入字节数组缓存，
     * 后续 getInputStream() 基于缓存返回，可重复读取
     * @param request 原始请求
     * @throws IOException 读取原始请求体流失败时抛出
     */
    public BodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        isSupportParseBody = canParseBody(request);
        if (isSupportParseBody){
            body = IOUtils.toByteArray(request.getInputStream());
            inputStream = new RequestCachingInputStream(body);
        }
    }

    /**
     * 只有json格式的数据接收body
     * @param request
     * @return
     */
    private boolean canParseBody(HttpServletRequest request){
        String contentType = request.getContentType();
        return StringUtils.hasText(contentType) && org.springframework.util.StringUtils.startsWithIgnoreCase(contentType, "application/json");
    }

    /**
     * @return
     */
    public byte[] getBody() {
        return body;
    }
    /**
     * 不支持解析 body 时返回原始请求的输入流；否则返回基于缓存字节数组的输入流，可重复读取
     * @return 请求体输入流
     * @throws IOException 获取原始输入流失败时抛出
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isSupportParseBody){
            return super.getInputStream();
        }
        return inputStream;
    }

    /*@Override
    public BufferedReader getReader() throws IOException {
        if (!isSupportParseBody){
            return super.getReader();
        } else {
            if (reader != null){
                return reader;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, getCharacterEncoding()));
            return reader;
        }
    }*/

    private static class RequestCachingInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        public RequestCachingInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }
        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readlistener) {
        }

    }
}
