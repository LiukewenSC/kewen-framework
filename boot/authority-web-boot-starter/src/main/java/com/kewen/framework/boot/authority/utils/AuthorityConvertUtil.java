package com.kewen.framework.boot.authority.utils;

import com.kewen.common.model.Dept;
import com.kewen.common.model.Permission;
import com.kewen.common.model.Position;
import com.kewen.common.model.Role;
import com.kewen.common.model.User;
import com.kewen.common.model.UserDetail;
import com.kewen.framework.boot.authority.model.Authority;
import com.kewen.framework.boot.authority.model.AuthorityObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
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

    public static Collection<Authority> parseCurrentUser(UserDetail userDetail){
        HashSet<Authority> set = new HashSet<>();
        Optional.ofNullable(userDetail.getDepts()).ifPresent(d-> d.forEach(r -> set.add(to(r))));
        Optional.ofNullable(userDetail.getRoles()).ifPresent(d-> d.forEach(r -> set.add(to(r))));
        Optional.ofNullable(userDetail.getPermissions()).ifPresent(d-> d.forEach(r -> set.add(to(r))));
        return set;
    }


    /**
     * entity 转换为 Authority
     */
    private static Authority to(User user){
        return new Authority(USER_PREFIX + user.getId(), USER_PREFIX + user.getName());
    }
    private static Authority to(Dept dept){
        return new Authority(DEPT_PREFIX + dept.getId(), DEPT_PREFIX + dept.getName());
    }
    private static Authority to(Position position){
        return new Authority(POSITION_PREFIX + position.getId(), POSITION_PREFIX + position.getName());
    }
    private static Authority to(Role role){
        return new Authority(ROLE_PREFIX + role.getId(), ROLE_PREFIX + role.getName());
    }
    private static Authority to(Permission permission){
        return new Authority(PERMISSION_PREFIX + permission.getId(), PERMISSION_PREFIX + permission.getName());
    }


    /**
     * 转换为权限结构对象，一般供前前端使用查看返回值
     * @param originals
     * @return
     */
    public static AuthorityObject parse(Collection<Authority> originals){

        //按照顺序排序，保证最后返回的数据是按照id顺序排列的
        List<Authority> auths = originals.stream()
                .sorted(Comparator.comparing(Authority::getAuthority))
                .collect(Collectors.toList());

        Collection<User> users= new ArrayList<>();
        Collection<Dept> depts=new ArrayList<>();
        Collection<Position> positions=new ArrayList<>();
        Collection<Role> roles=new ArrayList<>();
        Collection<Permission> permissions=new ArrayList<>();
        for (Authority auth : auths) {
            String authority = auth.getAuthority();
            String[] split = authority.split(SPLIT);
            String type = split[0];
            switch (type){
                case USER_CODE:
                    users.add(new User(Long.valueOf(split[1]),split[2]));
                    break;
                case DEPT_CODE:
                    depts.add(new Dept(Integer.valueOf(split[1]),split[2]));
                case POSITION_CODE:
                    positions.add( new Position(Integer.valueOf(split[1]),split[2]));
                case ROLE_CODE:
                    roles.add(new Role(Integer.valueOf(split[1]),split[2]));
                case PERMISSION_CODE:
                    permissions.add(new Permission(Integer.valueOf(split[1]),split[2]));
            }
        }
        AuthorityObject object = new AuthorityObject();
        if (!CollectionUtils.isEmpty(users)){
            object.setUsers(users);
        }
        if (!CollectionUtils.isEmpty(depts)){
            object.setDepts(depts);
        }
        if (!CollectionUtils.isEmpty(positions)){
            object.setPositions(positions);
        }
        if (!CollectionUtils.isEmpty(roles)){
            object.setRoles(roles);
        }
        if (!CollectionUtils.isEmpty(permissions)){
            object.setPermissions(permissions);
        }
        return object;
    }

    /**
     * 转换为实体对象
     * @param auth
     * @param tClass
     * @param <T>
     * @return
     */
    private static <T> T parse(Authority auth,Class<T> tClass){
        String authority = auth.getAuthority();
        String[] split = authority.split(SPLIT);
        String type = split[0];
        try {
            if (User.class.isAssignableFrom(tClass) && USER_CODE.equals(type)){
                return (T) new User(Long.valueOf(split[1]),split[2]);
            } else if (Dept.class.isAssignableFrom(tClass) && DEPT_CODE.equals(type)){
                return (T) new Dept(Integer.valueOf(split[1]),split[2]);
            } else if (Position.class.isAssignableFrom(tClass) && POSITION_CODE.equals(type)){
                return (T) new Position(Integer.valueOf(split[1]),split[2]);
            }else if (Role.class.isAssignableFrom(tClass) && ROLE_CODE.equals(type)){
                return (T) new Role(Integer.valueOf(split[1]),split[2]);
            }else if (Permission.class.isAssignableFrom(tClass) && PERMISSION_CODE.equals(type)){
                return (T) new Permission(Integer.valueOf(split[1]),split[2]);
            }
            throw new ClassCastException();
        } catch (Exception e) {
            String message = String.format("对象装换异常，auth: %s ，Class：%s ", auth, tClass.getName());
            log.error(message,e);
            throw new ClassCastException(message);
        }

    }






}
