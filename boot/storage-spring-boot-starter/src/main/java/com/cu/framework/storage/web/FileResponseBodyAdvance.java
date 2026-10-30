package com.cu.framework.storage.web;

import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import com.cu.framework.storage.core.model.FileFillSupport;
import com.cu.framework.storage.core.model.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件加载器，返回是若实现了 FileFill 则会将信息组装到返回对象中，
 * {@link FileFillSupport}
 *
 * @author kewen
 * @since 2023-04-28
 */
@Slf4j
@ControllerAdvice
@Order(8000)
public class FileResponseBodyAdvance implements ResponseBodyAdvice<Result> {

    @Autowired
    FileStorageProcessor fileStorageProcessor;

    /**
     * 仅拦截返回类型为 Result 及其子类的接口
     * @param returnType 控制器方法返回类型
     * @param converterType 消息转换器类型
     * @return 返回类型可赋值给 Result 时为 true
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * 响应体写出前回调：对 Result.data 执行文件信息填充后原样返回
     * @param body 待写出的响应体
     * @param returnType 控制器方法返回类型
     * @param selectedContentType 响应内容类型
     * @param selectedConverterType 选用的消息转换器类型
     * @param request 当前请求
     * @param response 当前响应
     * @return 填充文件信息后的响应体
     */
    @Override
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        fillFile(body.getData());

        return body;

    }

    /**
     *  填充文件
     * @param data
     */
    private void fillFile(Object data){
        if (data==null){
            return;
        }

        // key； 需要组装的对象 value:对应的fileId ，找到之后将对应的对象加入到其中，后续可以设值
        List<FileFillSupport> needFills = new ArrayList<>();


        //1返回对象为List的情况
        if (data instanceof Collection) {
            for (Object datum : ((Collection<?>) data)) {
                fetchFileFill(needFills, datum);
            }
        }
        //2 返回对象为Page的情况
        else if (data instanceof PageResult) {
            List<?> records = ((PageResult<?>) data).getData();
            if (records != null){
                for (Object record : records) {
                    fetchFileFill(needFills, record);
                }
            }
        }
        //3 返回对象直接为 Object的
        else {
            fetchFileFill(needFills, data);
        }

        // 组装值
        if (!needFills.isEmpty()) {
            //查询到文件列表
            List<Long> ids = needFills.stream().map(FileFillSupport::getFileId).collect(Collectors.toList());
            List<FileInfo> fileInfos = fileStorageProcessor.listDownloadInfo(ids);
            //变化成map方便取用
            Map<Long, FileInfo> fileInfoMap = new HashMap<>();
            for (FileInfo fileInfo : fileInfos) {
                if (fileInfo ==null){
                    continue;
                }
                fileInfoMap.putIfAbsent(fileInfo.getFileId(), fileInfo);
            }
            //循环组装
            for (FileFillSupport needFill : needFills) {
                Long fileId = needFill.getFileId();
                if (fileId==null){
                    continue;
                }
                FileInfo fileInfo = fileInfoMap.get(fileId);
                if (fileInfo==null){
                    continue;
                }
                needFill.setFileInfo(fileInfo);
            }
        }
    }

    /**
     *  获取对象是否有实现 FileFillSupport 或 属性是否实现FileFillSupport，加入待组装集合中
     * @param needFills
     * @param data
     */
    private void fetchFileFill(List<FileFillSupport> needFills, Object data) {
        try {
            if (data instanceof FileFillSupport) {
                FileFillSupport fileFill = (FileFillSupport) data;
                needFills.add(fileFill);
            }

            //设置值
            Field[] fields = data.getClass().getDeclaredFields();


            for (Field field : fields) {
                Type genericType = field.getGenericType();
                //对象型，可以直接获取到类型
                if (FileFillSupport.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object o = field.get(data);
                    if (o != null) {
                        needFills.add(((FileFillSupport) o));
                    }

                }
                // ParameterizedTypeImpl 带泛型型（泛型可以是对象泛型，也可以是集合泛型，这里主要处理集合泛型），需要将集合拿出
                else if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;

                    //判定是不是集合，是集合则继续执行
                    if (parameterizedType.getRawType() instanceof Class
                            && Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                        //获取实际的泛型，集合因只有一个泛型，则直接取[0]位置判断泛型是否符合文件类型
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        Type actualTypeArgument = actualTypeArguments[0];
                        if (actualTypeArgument instanceof Class) {
                            if (FileFillSupport.class.isAssignableFrom(((Class<?>) actualTypeArgument))) {
                                field.setAccessible(true);
                                Object o = field.get(data);
                                for (Object sub : ((Collection) o)) {
                                    needFills.add(((FileFillSupport) sub));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log.error("获取文信息件异常", e);
        }
    }
}
