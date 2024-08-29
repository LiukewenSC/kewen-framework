package com.kewen.framework.storage.boot;

import com.kewen.framework.basic.model.PageResult;
import com.kewen.framework.basic.model.Result;
import com.kewen.framework.storage.model.FileFillSupport;
import com.kewen.framework.storage.model.FileInfo;
import com.kewen.framework.storage.web.service.StorageService;
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
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    StorageService storageService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

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
            List<?> records = ((PageResult<?>) data).getRecords();
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
            List<FileInfo> fileInfos = storageService.listDownloadInfo(ids);
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
                else if (genericType instanceof ParameterizedTypeImpl) {
                    ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) genericType;

                    //判定是不是集合，是集合则继续执行
                    if (Collection.class.isAssignableFrom(parameterizedType.getRawType())) {
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
