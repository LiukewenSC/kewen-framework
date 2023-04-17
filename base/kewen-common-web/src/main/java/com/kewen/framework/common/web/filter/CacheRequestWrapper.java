package com.kewen.framework.common.web.filter;

import org.apache.commons.io.IOUtils;

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
 * @author liukewen
 * @since 2022/12/29
 */
public class CacheRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    private BufferedReader reader;

    private ServletInputStream inputStream;

    public CacheRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = IOUtils.toByteArray(request.getInputStream());
        inputStream = new RequestCachingInputStream(body);
    }

    /**
     * 只读取一次，保证可以不多占用内存
     * @return
     */
    public byte[] getBodyOnce() {
        byte[] clone = body.clone();
        body=null;
        return clone;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        return super.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(inputStream, getCharacterEncoding()));
        }
        return reader;
    }

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
