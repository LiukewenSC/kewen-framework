package com.cu.framework.sample.authcore.web;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 方法级管辖声明：注解标在方法上，只管辖那一个端点，同一个类里其他端点不受影响。
 * <p>
 * 另外两个端点用了完全相同的显示文案，但仍是两条独立的菜单行、各自独立的授权
 *
 * @author kewen
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/methodLevel")
public class MethodLevelController {

    /**
     * 受管辖且带显示文案：alice（ROLE__10）可进，bob 与无身份者被拒
     *
     * @return 固定文本
     */
    @AuthMenu
    @AuthMenuDoc(name = "方法级管辖端点")
    @GetMapping("/guarded")
    public Result<String> guarded() {
        return Result.success("guarded-ok");
    }

    /**
     * 同一个类里没有声明的端点：不做菜单校验，任何人都能进
     *
     * @return 固定文本
     */
    @GetMapping("/free")
    public Result<String> free() {
        return Result.success("free-ok");
    }

    /**
     * 有管辖声明但没有显示文案：菜单行标签回落到方法名
     *
     * @return 固定文本
     */
    @AuthMenu
    @GetMapping("/noDoc")
    public Result<String> noDoc() {
        return Result.success("no-doc-ok");
    }

    /**
     * 同名文案 A：授给了 ROLE__20
     *
     * @return 固定文本
     */
    @AuthMenu
    @AuthMenuDoc(name = "同名文案的两行")
    @GetMapping("/sameDocA")
    public Result<String> sameDocA() {
        return Result.success("same-doc-a-ok");
    }

    /**
     * 同名文案 B：谁都没授。证明文案相同不会让两行共用授权
     *
     * @return 固定文本
     */
    @AuthMenu
    @AuthMenuDoc(name = "同名文案的两行")
    @GetMapping("/sameDocB")
    public Result<String> sameDocB() {
        return Result.success("same-doc-b-ok");
    }
}
