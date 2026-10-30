package com.cu.framework.auth.core.menu;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.exception.AuthException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 菜单管辖判定测试：类上声明即覆盖该类全部端点，方法声明只覆盖该端点，两处都没有则不校验。
 *
 * @author kewen
 * @since 2026-10-20
 */
public class MenuAccessGovernanceTest {

    /** 只在类上声明，方法全部受批量管辖 */
    @AuthMenu
    public static class ClassLevelController {
        public void list() {
        }
    }

    /** 只在方法上声明 */
    public static class MethodLevelController {
        @AuthMenu
        public void list() {
        }

        public void other() {
        }
    }

    /** 两处都没有声明 */
    public static class UngovernedController {
        public void list() {
        }
    }

    /** 记录被校验过的路径，按预设结果放行或拒绝 */
    private static class RecordingMenuHandler implements AuthMenuHandler {
        private final List<String> checked = new ArrayList<>();
        private boolean accessible = true;

        @Override
        public boolean hasMenuAccessAuth(Collection<FlatteningAuthority> auths, String path) {
            checked.add(path);
            return accessible;
        }
    }

    private static class EmptyContext implements AuthCurrentUserContextHolder {
        @Override
        public CurrentUser getUser() {
            return null;
        }

        @Override
        public Collection<FlatteningAuthority> getAuths() {
            return Collections.emptyList();
        }
    }

    @Test
    public void classLevelDeclarationGovernsEveryEndpointOfThatClass() {
        RecordingMenuHandler menuHandler = new RecordingMenuHandler();
        boolean passed = interceptor(menuHandler).preHandle(request("/x/list"), null, handler(ClassLevelController.class, "list"));
        Assert.assertTrue(passed);
        Assert.assertEquals(Collections.singletonList("/x/list"), menuHandler.checked);
    }

    @Test
    public void methodLevelDeclarationGovernsOnlyThatEndpoint() {
        RecordingMenuHandler menuHandler = new RecordingMenuHandler();
        MenuAccessInterceptor interceptor = interceptor(menuHandler);

        Assert.assertTrue(interceptor.preHandle(request("/x/list"), null, handler(MethodLevelController.class, "list")));
        Assert.assertEquals(Collections.singletonList("/x/list"), menuHandler.checked);

        Assert.assertTrue(interceptor.preHandle(request("/x/other"), null, handler(MethodLevelController.class, "other")));
        Assert.assertEquals("未声明的端点不应进入菜单校验", 1, menuHandler.checked.size());
    }

    @Test
    public void ungovernedControllerIsNotChecked() {
        RecordingMenuHandler menuHandler = new RecordingMenuHandler();
        Assert.assertTrue(interceptor(menuHandler).preHandle(request("/x/list"), null, handler(UngovernedController.class, "list")));
        Assert.assertTrue(menuHandler.checked.isEmpty());
    }

    @Test(expected = AuthException.class)
    public void governedEndpointWithoutAccessIsRejected() {
        RecordingMenuHandler menuHandler = new RecordingMenuHandler();
        menuHandler.accessible = false;
        interceptor(menuHandler).preHandle(request("/x/list"), null, handler(ClassLevelController.class, "list"));
    }

    @Test
    public void nonHandlerMethodPassesThrough() {
        RecordingMenuHandler menuHandler = new RecordingMenuHandler();
        Assert.assertTrue(interceptor(menuHandler).preHandle(request("/x/list"), null, "not-a-handler"));
        Assert.assertTrue(menuHandler.checked.isEmpty());
    }

    private MenuAccessInterceptor interceptor(AuthMenuHandler menuHandler) {
        return new MenuAccessInterceptor(menuHandler, new EmptyContext());
    }

    private HandlerMethod handler(Class<?> controller, String methodName) {
        try {
            return new HandlerMethod(controller.getDeclaredConstructor().newInstance(), methodName);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("构造被测 handler 失败", e);
        }
    }

    private HttpServletRequest request(String uri) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                MenuAccessGovernanceTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getRequestURI":
                            return uri;
                        case "getContextPath":
                            return "";
                        default:
                            return null;
                    }
                });
    }
}
