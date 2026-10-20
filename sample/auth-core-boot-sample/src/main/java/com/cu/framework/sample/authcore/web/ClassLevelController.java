package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类级管辖声明：一次声明管辖本类全部端点，边界不越出这个类。
 * <p>
 * 两个端点各自有独立授权：all 授给了 ROLE__10，two 谁都没授——
 * 类级声明决定"管不管"，具体能不能进仍由每个路径自己的授权决定
 *
 * @author kewen
 * @since 2026-10-20
 */
@AuthMenu
@AuthMenuDoc(name = "类级管辖分组")
@RestController
@RequestMapping("/classLevel")
public class ClassLevelController {

    /**
     * 受管辖且已授予 ROLE__10
     *
     * @return 固定文本
     */
    @AuthMenuDoc(name = "类级下的端点一")
    @GetMapping("/all")
    public Result<String> all() {
        return Result.success("class-level-all-ok");
    }

    /**
     * 同样受管辖，但没有任何权限被授予过，任何人都进不来
     *
     * @return 固定文本
     */
    @AuthMenuDoc(name = "类级下的端点二")
    @GetMapping("/two")
    public Result<String> two() {
        return Result.success("class-level-two-ok");
    }
}
