package com.kewen.framework.sample.feign;

import com.kewen.framework.common.core.model.Result;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloFeignFallbackFactory implements FallbackFactory<HelloFeign> {

    @Override
    public HelloFeign create(Throwable throwable) {
        return new HelloFeign() {
            @Override
            public Result<String> hello() {
                log.info("HelloFeign hello 服务降级处理");
                return Result.success("HelloFeign hello 服务降级处理 结果");
            }
        };
    }
}
