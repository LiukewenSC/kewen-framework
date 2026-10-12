package controller.security;


import com.cu.framework.basic.model.Result;
import com.cu.framework.auth.security.annotation.SecurityIgnore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityIgnore
@RequestMapping("/hello")
public class TestSecurityIgnoreController {

    @RequestMapping("/hello")
    public Result<String> hello(){
        return Result.success("SecurityIgnore method");
    }

}
