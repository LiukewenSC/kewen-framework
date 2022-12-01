package com.kewen.common.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description  
 * @author LiuKewen
 * @date 2022/1/6 12:51
 */
public class TreeUtil {
    private TreeUtil() {
    }

    /**
     * 转换树形结构
     * @param source 需要组装的数据
     * @param <P> 第一层数据的parentId，
     *           eg1 id="1" parentId=null ,topParentId=null 则 此数据为第一层数据
     *           eg2 id="1" parentId="i1" ,topParentId=i1 则 此数据为第一层数据
     *           eg3 id="1" parentId="i1" ,topParentId=null 则 此数据不为第一层数据
     * @param <T> 泛型
     * @return 分层之后的列表
     */
    public static <P,T extends TreeBase<T,P>> List<T> transfer(List<T> source,P topParentId){
        ArrayList<T> ts = new ArrayList<>(source);
        Iterator<T> iterator = ts.iterator();

        List<T> root = new ArrayList<>();
        //移除根数据
        while (iterator.hasNext()){
            T next = iterator.next();
            if (next.isTop(topParentId)){
                root.add(next);
                iterator.remove();
            }
        }
        /* todo 检测环路 貌似不需要检测环路
        List<String> combine = ts.stream().map(
                t -> t.getParentId() == null ? t.getId().toString() : t.getId().toString().concat(t.getParentId().toString())
        ).collect(Collectors.toList());
        boolean hasLoop = ts.stream()
                .filter(t -> t.getParentId() != null)
                .anyMatch(t -> combine.contains(t.getParentId().toString().concat(t.getId().toString())));
        if (hasLoop){
            throw new StackOverflowError("数据集有递归,无法组装树形结构");
        }*/

        Map<P, List<T>> parentMap = ts.stream()
                .filter(m->m.getParentId()!=null)
                .collect(Collectors.groupingBy(T::getParentId));
        for (T t : ts) {
            //设置子菜单（引用，因此会将相关数据加入其中，但是不得有循环，否则栈溢出）
            t.setSubs(parentMap.get(t.getId()));
        }
        return root.stream().peek(r -> r.setSubs(parentMap.get(r.getId()))).collect(Collectors.toList());
    }

    /**
     * @description 树形对象
     * @author LiuKewen
     * @date 2022/1/6 12:52
     */
    public interface TreeBase<T, ID> {

        /**
         * 当前id
         * @return
         */
        ID getId();

        /**
         * 父id
         * @return
         */
        ID getParentId();

        /**
         * 是否是顶级菜单 一般顶级菜单的父菜单为null或者 0 或者自己定义的
         * @param p
         * @return
         */
        default boolean isTop(ID p) {
            return p == null;
        }

        void setSubs(List<T> subs);
    }
}