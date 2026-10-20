package com.cu.framework.sample.authcore.web;

import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 完全没有管辖声明的 Controller：方法与类上都没有，请求进来不执行任何菜单校验。
 * 这是"未声明即不管辖"的对照组
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/open")
public class OpenController {

    /**
     * 任何人（包括不带身份头的请求）都能进
     *
     * @return 固定文本
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("open-ping");
    }
}
