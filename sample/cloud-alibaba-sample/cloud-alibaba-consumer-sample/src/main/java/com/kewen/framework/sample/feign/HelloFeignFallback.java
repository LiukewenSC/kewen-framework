package com.kewen.framework.sample.feign;

import com.kewen.framework.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class HelloFeignFallback implements HelloFeign {
    @Override
    public Result<String> hello() {
        log.info("HelloFeign hello 服务降级处理");
        return Result.success("HelloFeign hello 服务降级处理 结果");
    }
}
