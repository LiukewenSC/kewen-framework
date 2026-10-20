package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类级管辖但<b>没有</b>任何显示文案：分组行的名称回落到类简单名，
 * 端点行的名称回落到方法名。显示文案不参与鉴权，只是标签
 *
 * @author kewen
 * @since 2026-10-20
 */
@AuthMenu
@RestController
@RequestMapping("/classNoDoc")
public class ClassNoDocController {

    /**
     * 受管辖，标签由方法名回落得到
     *
     * @return 固定文本
     */
    @GetMapping("/all")
    public Result<String> all() {
        return Result.success("class-no-doc-ok");
    }
}
