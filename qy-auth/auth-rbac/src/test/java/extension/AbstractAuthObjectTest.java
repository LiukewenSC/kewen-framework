package extension;


import com.alibaba.fastjson.JSONObject;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.rbac.composite.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class AbstractAuthObjectTest {

    /**
     * 测试AuthObject和BaseAuth的转换
     */
    @Test
    public  void testAuthObject2BaseAuth(){
        SimpleAuthObject object = new SimpleAuthObject();
        object.addUsers(new User(1L,"用户1"),new User(2L,"用户2"),new User(3L,"用户3"));
        object.addDepts(new DeptWithParents(1L,"部门1"),new DeptWithParents(2L,"部门2"),new DeptWithParents(3L,"部门3"));
        object.addRoles(new Role(1L,"角色1"),new Role(2L,"角色2"),new Role(3L,"角色3"));
        object.addDeptRoles(
                new DeptRole(new Dept(11L,"部门11"),new Role(11L,"角色11")),
                new DeptRole(new Dept(12L,"部门12"),new Role(12L,"角色12"))
        );

        Collection<FlatteningAuthority> flatteningAuthorities = object.listBaseAuth();
        Assert.assertEquals(flatteningAuthorities.size(),11);


        SimpleAuthObject simpleAuthObject = new SimpleAuthObject();

        simpleAuthObject.setProperties(flatteningAuthorities);
        Assert.assertNotNull(simpleAuthObject.getUsers().get(0).getId());
        Assert.assertNotNull(simpleAuthObject.getDepts().get(0).getId());
        Assert.assertNotNull(simpleAuthObject.getRoles().get(0).getId());
        Assert.assertEquals(11L, (long) simpleAuthObject.getDeptRoles().get(0).getDept().getId());
        Assert.assertEquals(11L, (long) simpleAuthObject.getDeptRoles().get(0).getRole().getId());
        System.out.println(JSONObject.toJSONString(simpleAuthObject,true));
    }

}