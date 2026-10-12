package controller.authmenu;



import com.cu.framework.auth.core.AuthMenu;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthMenuMethodController {


    @GetMapping("/testAuthMenuMethod")
    @AuthMenu(name = "测试只在Method上加@AuthMenu权限注解")
    public Object testDataRange() {
        return "";
    }
}
