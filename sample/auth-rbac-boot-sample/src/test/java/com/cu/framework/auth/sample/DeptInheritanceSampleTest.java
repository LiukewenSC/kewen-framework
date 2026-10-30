package com.cu.framework.auth.sample;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.model.UserAuthInfo;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 部门继承权限的端到端对照：菜单上只挂一条 {@code RELATIONDEPT__2}（总部），
 * 三个身份的部门归属分别是总部的成员、总部的子部门成员、与总部无层级关系的部门成员。
 * <p>
 * 判据取真实请求的响应体 code（200 放行 / 403 拒绝），不是直调判定函数——
 * 直调只能证明判定认得这个串，证明不了从装载到拦截的整条通路
 *
 * @author kewen
 * @since 2026-10-22
 */
public class DeptInheritanceSampleTest extends AbstractRbacSampleWebTest {

    /** DeptInheritDemoController 的唯一端点 */
    private static final String DEMO_PATH = "/deptInherit/rooms";
    /** 声明本次请求以哪个用户装载权限，与 auth-core 示例同名 */
    private static final String USER_HEADER = "X-Demo-User";
    /** 种子数据里演示端点对应的菜单行 */
    private static final long DEMO_API_ID = 9100000000000000002L;
    /** 被授权挂在这个部门上，它的子孙部门成员才该进来 */
    private static final long GRANTED_DEPT_ID = 2L;

    private static final int ALLOWED = 200;
    private static final int DENIED = 403;

    @Autowired
    private SysUserComposite sysUserComposite;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 以指定用户身份请求演示端点，返回响应体里的结果码
     */
    private int codeAs(String username) {
        try {
            JsonNode json = performJson(get(DEMO_PATH).header(USER_HEADER, username));
            return json.path("code").asInt(-1);
        } catch (Exception e) {
            throw new IllegalStateException("请求演示端点 " + DEMO_PATH + " 失败", e);
        }
    }

    @Test
    public void subordinateDeptMemberIsAllowed() {
        // subordinate 在研发部(3)，研发部的上级链含总部(2)
        Assertions.assertEquals(ALLOWED, codeAs("subordinate"), "总部的子部门成员应被这条继承授权放行");
    }

    @Test
    public void ownDeptMemberIsDenied() {
        // admin2 本人就在总部(2)：继承标记不含本部门，所以它不该因为"配在自己部门上"而进来
        Assertions.assertEquals(DENIED, codeAs("admin2"), "总部自己的成员不在继承授权的覆盖范围内");
    }

    @Test
    public void unrelatedDeptMemberIsDenied() {
        // outsider 在外部合作部(4)，那是一棵独立的根，与总部没有层级关系
        Assertions.assertEquals(DENIED, codeAs("outsider"));
    }

    @Test
    public void requestWithoutIdentityHeaderFallsBackToAdmin() throws Exception {
        // admin 在根部门(1)，是总部的上级而不是子孙
        JsonNode json = performJson(get(DEMO_PATH));
        Assertions.assertEquals(DENIED, json.path("code").asInt(-1), "不带身份头时缺省是 admin，它不在总部的子孙里");
    }

    @Test
    public void grantingTheDeptsOwnFlagAdmitsItsOwnMember() {
        // 把判据钉在层级关系上而不是常量上：同一身份、同一端点，
        // 额外补一条"本部门"标记后，总部的成员就该进得来——继承标记本身做不到这一点
        try {
            jdbcTemplate.update("INSERT INTO sys_auth_menu (id, api_id, authority, description) VALUES (9999, ?, ?, ?)",
                    DEMO_API_ID, "DEPT__" + GRANTED_DEPT_ID, "DEPT__总部");
            Assertions.assertEquals(ALLOWED, codeAs("admin2"), "补上本部门标记后同一条授权就该放行本部门成员");
        } finally {
            jdbcTemplate.update("DELETE FROM sys_auth_menu WHERE id = 9999");
        }
    }

    @Test
    public void multiDeptUserLoadsOneAuthInfoCarryingBothDepts() {
        // admin2 挂在总部(2) 与外部合作部(4)：一次 join 出多行，必须聚合成一个权限体而不是抛 TooManyResults，
        // 且映射修正后用户部分真的被灌上了值
        UserAuthObject object = sysUserComposite.getUserAuthObjectByUsername("admin2");
        Assertions.assertNotNull(object);
        UserAuthInfo authInfo = object.getAuthInfo();
        Assertions.assertNotNull(authInfo, "有凭证的用户应装载出权限体");
        Assertions.assertNotNull(authInfo.getUser(), "映射到单个用户后 user 不再是空");
        Assertions.assertEquals(Long.valueOf(2L), authInfo.getUser().getId());
        Assertions.assertEquals(2, authInfo.getDepts().size(), "两个部门都要装进同一个权限体");

        Set<String> auths = authStrings(authInfo);
        Assertions.assertTrue(auths.contains("DEPT__2"), auths.toString());
        Assertions.assertTrue(auths.contains("DEPT__4"), auths.toString());
        Assertions.assertTrue(auths.contains("RELATIONDEPT__1"), "总部自己的上级是根部门，所以根的继承串在");
        Assertions.assertFalse(auths.contains("RELATIONDEPT__2"), "但总部的继承串不该由总部成员带出来");
    }

    private static Set<String> authStrings(UserAuthInfo authInfo) {
        Set<String> strings = new HashSet<>();
        for (FlatteningAuthority authority : authInfo.listBaseAuth()) {
            strings.add(authority.getAuth());
        }
        return strings;
    }
}
