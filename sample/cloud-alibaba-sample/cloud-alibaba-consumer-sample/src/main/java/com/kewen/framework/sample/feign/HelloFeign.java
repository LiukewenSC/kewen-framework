package com.kewen.framework.sample.feign;

import com.kewen.framework.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name ="nocos-provider-sample" ,path = "/hello",
        //fallback = HelloFeignFallback.class,
        fallbackFactory = HelloFeignFallbackFactory.class )
public interface HelloFeign {

    @GetMapping("/hello")
    Result<String> hello();

}
