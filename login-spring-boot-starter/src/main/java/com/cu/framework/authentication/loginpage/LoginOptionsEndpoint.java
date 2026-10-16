package com.cu.framework.authentication.loginpage;

import com.cu.framework.authentication.annotation.SecurityIgnore;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录方式发现接口，供前端登录页按条渲染登录入口。
 * <p>
 * 前端由此不需要知道任何身份源的数量、标识与端点差异；配置增减一条，页面重新加载即反映，无需改前端代码。
 *
 * @author kewen
 * @since 2026-10-16
 */
@SecurityIgnore
@RestController
public class LoginOptionsEndpoint {

    private final LoginOptionsFactory loginOptionsFactory;

    public LoginOptionsEndpoint(LoginOptionsFactory loginOptionsFactory) {
        this.loginOptionsFactory = loginOptionsFactory;
    }

    /**
     * 查询当前可用的登录方式，未经认证即可访问
     *
     * @return 账号密码提交信息与单点登录入口列表
     */
    @GetMapping("/api/login/options")
    public Result<LoginOptions> options() {
        return Result.success(loginOptionsFactory.create());
    }
}
