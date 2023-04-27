package com.kewen.framework.common.web.filter.model;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 请求包装类，为了能够多读取一次数据流
 *  当不支持body参数解析的时候返回的body数组为空
 * @author liukewen
 * @since 2022/12/29
 */
public class BodyParsedHttpServletRequest extends HttpServletRequestWrapper {

    private final boolean isSupportParseBody;


    private ServletInputStream inputStream;
    private BufferedReader reader;
    private byte[] body;

    public BodyParsedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        isSupportParseBody = canParseBody(request);
        if (isSupportParseBody){
            body = IOUtils.toByteArray(request.getInputStream());
            inputStream = new RequestCachingInputStream(body);
        }
    }
    private boolean canParseBody(HttpServletRequest request){
        String contentType = request.getContentType();
        // multipart/form-data; boundary=--------------------------806095705637677561579964
        if (StringUtils.isNotBlank(contentType) && org.springframework.util.StringUtils.startsWithIgnoreCase(contentType, "multipart/")) {
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    public byte[] getBody() {
        return body;
    }
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