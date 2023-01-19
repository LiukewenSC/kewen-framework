package com.kewen.framework.sample.feign;

import com.kewen.framework.base.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class HelloFeignFallback implements HelloFeign {
    @Override
    public Result<String> hello() {
        log.info("HelloFeign hello 服务降级处理");
        return Result.success("HelloFeign hello 服务降级处理 结果");
    }
}
