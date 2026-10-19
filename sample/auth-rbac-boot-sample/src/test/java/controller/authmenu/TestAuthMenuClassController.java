package controller.authmenu;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testAnnoClassMenuController")
@AuthMenu
@AuthMenuDoc(name = "测试只在Controller上加@AuthMenu权限注解")
public class TestAuthMenuClassController {

    @GetMapping("/hello")
    public String hello(String name) {
        return "hello " + name;
    }
    @GetMapping("/hello2")
    public String hello2() {
        return "hello2 ";
    }
}
