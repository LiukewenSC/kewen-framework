package com.kewen.framework.base.authority.utils;

import com.kewen.framework.base.authority.model.Dept;
import com.kewen.framework.base.authority.model.Position;
import com.kewen.framework.base.authority.model.Role;
import com.kewen.framework.base.authority.model.SysAuthority;
import com.kewen.framework.base.common.model.User;
import com.kewen.framework.base.authority.model.SysUserDetail;
import com.kewen.framework.base.authority.model.SysAuthorityObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AuthorityConvertUtil {

    private static final String SPLIT="_";
    private static final String USER_CODE="U";
    private static final String USER_PREFIX=USER_CODE+SPLIT;
    private static final String DEPT_CODE="D";
    private static final String DEPT_PREFIX=DEPT_CODE+SPLIT;
    private static final String POSITION_CODE="Po";
    private static final String POSITION_PREFIX=POSITION_CODE+SPLIT;
    private static final String ROLE_CODE="R";
    private static final String ROLE_PREFIX=ROLE_CODE+SPLIT;
    private static final String PERMISSION_CODE="Pe";
    private static final String PERMISSION_PREFIX=PERMISSION_CODE+SPLIT;
    private static final String NONE="N"+SPLIT+"NONE";
    private static final String NONE_DESCRIPTION="N"+SPLIT+"无权限";
    /**
     * 无权限的返回对象格式
     * @return
     */
    private static SysAuthority noneAuthority(){
        return new SysAuthority(NONE,NONE_DESCRIPTION);
    }

    /**
     * 解析当前用户的权限
     * @param sysUserDetail
     * @return
     */
    public static Collection<SysAuthority> parseCurrentUser(SysUserDetail sysUserDetail){
        if (sysUserDetail ==null){
            return Collections.singletonList(noneAuthority());
        }
        HashSet<SysAuthority> set = new HashSet<>();
        Optional.ofNullable(sysUserDetail.allDepts()).ifPresent(d-> d.forEach(r -> set.add(to(r))));
        Optional.ofNullable(sysUserDetail.getRoles()).ifPresent(d-> d.forEach(r -> set.add(to(r))));
        return set;
    }

    /**
     * 将权限对象转换为权限字符串列表
     * @param sysAuthorityObject
     * @return
     */
    public static List<SysAuthority> to(SysAuthorityObject sysAuthorityObject){
        ArrayList<SysAuthority> authorities = new ArrayList<>();
        Collection<User> users = sysAuthorityObject.getUsers();
        if (!org.springframework.util.CollectionUtils.isEmpty(users)){
            authorities.addAll(users.stream().map(AuthorityConvertUtil::to).collect(Collectors.toList()));
        }
        Collection<Dept> depts = sysAuthorityObject.getDepts();
        if (!org.springframework.util.CollectionUtils.isEmpty(users)){
            authorities.addAll(depts.stream().map(AuthorityConvertUtil::to).collect(Collectors.toList()));
        }
        Collection<Role> roles = sysAuthorityObject.getRoles();
        if (!org.springframework.util.CollectionUtils.isEmpty(roles)){
            authorities.addAll(roles.stream().map(AuthorityConvertUtil::to).collect(Collectors.toList()));
        }
        Collection<Position> positions = sysAuthorityObject.getPositions();
        if (!org.springframework.util.CollectionUtils.isEmpty(positions)){
            authorities.addAll(positions.stream().map(AuthorityConvertUtil::to).collect(Collectors.toList()));
        }
        return authorities;
    }


    /**
     * entity 转换为 Authority
     */
    private static SysAuthority to(User user){
        return new SysAuthority(USER_PREFIX + user.getId(), USER_PREFIX + user.getName());
    }
    private static SysAuthority to(Dept dept){
        return new SysAuthority(DEPT_PREFIX + dept.getId(), DEPT_PREFIX + dept.getName());
    }
    private static SysAuthority to(Position position){
        return new SysAuthority(POSITION_PREFIX + position.getId(), POSITION_PREFIX + position.getName());
    }
    private static SysAuthority to(Role role){
        return new SysAuthority(ROLE_PREFIX + role.getId(), ROLE_PREFIX + role.getName());
    }


    /**
     * 转换为权限结构对象，一般供前前端使用查看返回值
     * @param originals
     * @return
     */
    public static SysAuthorityObject parse(Collection<SysAuthority> originals){

        //按照顺序排序，保证最后返回的数据是按照id顺序排列的
        List<SysAuthority> auths = originals.stream()
                .sorted(Comparator.comparing(SysAuthority::getAuthority))
                .collect(Collectors.toList());

        Collection<User> users= new ArrayList<>();
        Collection<Dept> depts=new ArrayList<>();
        Collection<Position> positions=new ArrayList<>();
        Collection<Role> roles=new ArrayList<>();
        for (SysAuthority auth : auths) {
            String authority = auth.getAuthority();
            String description = auth.getDescription();
            String[] split = authority.split(SPLIT);
            String[] splitDescription = description.split(SPLIT);
            String type = split[0];
            switch (type){
                case USER_CODE:
                    users.add(new User(Long.valueOf(split[1]),splitDescription[1]));
                    break;
                case DEPT_CODE:
                    depts.add(new Dept(Long.valueOf(split[1]),splitDescription[1]));
                    break;
                case POSITION_CODE:
                    positions.add(new Position(Long.valueOf(split[1]),splitDescription[1]));
                    break;
                case ROLE_CODE:
                    roles.add(new Role(Long.valueOf(split[1]),splitDescription[1]));
                    break;
                default:
            }
        }
        SysAuthorityObject object = new SysAuthorityObject();
        if (!org.springframework.util.CollectionUtils.isEmpty(users)){
            object.setUsers(users);
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(depts)){
            object.setDepts(depts);
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(positions)){
            object.setPositions(positions);
        }
        if (!CollectionUtils.isEmpty(roles)){
            object.setRoles(roles);
        }
        return object;
    }

}
