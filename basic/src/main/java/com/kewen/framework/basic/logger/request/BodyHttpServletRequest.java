package com.kewen.framework.basic.logger.request;

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

    private final boolean isSupportParseBody;


    private ServletInputStream inputStream;
    private BufferedReader reader;
    private byte[] body;

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
