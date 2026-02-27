package com.kewen.framework.idaas.saml;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * SAML2 GET 方式退出 Controller
 * <p>
 * Spring Security 的 /logout 默认只接受 POST 请求（CSRF 保护），
 * 此 Controller 提供 GET /saml2/logout 接口，返回一个自动提交的 POST 表单页面，
 * 浏览器加载后自动 POST 到 /logout，触发 SAML2 SLO 流程。
 *
 * @author kewen
 * @since 1.0
 */
@Controller
public class SamlLogoutController {

    /**
     * GET 方式触发退出，返回自动提交 POST 表单的 HTML 页面
     */
    @GetMapping("/saml2/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String logoutUrl = request.getContextPath() + "/logout";

        // 获取 CSRF token（如果启用了 CSRF）
        String csrfInput = "";
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfInput = "<input type=\"hidden\" name=\"" + csrfToken.getParameterName()
                    + "\" value=\"" + csrfToken.getToken() + "\"/>";
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("<!DOCTYPE html><html><head><title>Logging out...</title></head><body>");
        writer.write("<form id=\"logoutForm\" method=\"POST\" action=\"" + logoutUrl + "\">");
        writer.write(csrfInput);
        writer.write("</form>");
        writer.write("<script>document.getElementById('logoutForm').submit();</script>");
        writer.write("</body></html>");
        writer.flush();
    }
}
