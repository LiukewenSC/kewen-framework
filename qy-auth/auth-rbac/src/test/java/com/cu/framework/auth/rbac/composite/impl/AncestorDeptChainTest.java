package com.cu.framework.auth.rbac.composite.impl;

import com.cu.framework.auth.rbac.composite.model.ParentDept;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 严格上级链的展开顺序、层级口径与畸形数据的终止行为。
 * <p>
 * 仓库内不引入 mock 库，故用 Map 直接充当部门查询，让遍历能脱离数据库被断言
 *
 * @author kewen
 * @since 2026-10-22
 */
public class AncestorDeptChainTest {

    private static List<Long> idsOf(List<ParentDept> chain) {
        List<Long> ids = new ArrayList<>(chain.size());
        for (ParentDept parent : chain) {
            ids.add(parent.getId());
        }
        return ids;
    }

    private static List<Integer> levelsOf(List<ParentDept> chain) {
        List<Integer> levels = new ArrayList<>(chain.size());
        for (ParentDept parent : chain) {
            levels.add(parent.getLevel());
        }
        return levels;
    }

    @Test
    public void chainExcludesSelfAndIsRootFirst() {
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(
                DeptTreeFixture.GROUP, DeptTreeFixture.fourLevelTree());
        Assert.assertEquals("不含组自己，且根在前、直接上级在后",
                Arrays.asList(DeptTreeFixture.ROOT, DeptTreeFixture.CENTER, DeptTreeFixture.SECTION),
                idsOf(chain));
    }

    @Test
    public void directParentIsTheLastElement() {
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(
                DeptTreeFixture.GROUP, DeptTreeFixture.fourLevelTree());
        Assert.assertEquals("末位就是直接上级，消费方不需要再猜顺序",
                Long.valueOf(DeptTreeFixture.SECTION), chain.get(chain.size() - 1).getId());
    }

    @Test
    public void levelCountsDistanceFromSelf() {
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(
                DeptTreeFixture.GROUP, DeptTreeFixture.fourLevelTree());
        // 链是根在前，所以层级倒着数：根距自身 3 级、隔级 2 级、直接上级 1 级
        Assert.assertEquals(Arrays.asList(3, 2, 1), levelsOf(chain));
    }

    @Test
    public void rootDeptHasNoAncestor() {
        Assert.assertTrue("根部门没有可继承的上级",
                SysUserCompositeImpl.listAncestorDepts(
                        DeptTreeFixture.ROOT, DeptTreeFixture.fourLevelTree()).isEmpty());
    }

    @Test
    public void missingSelfRecordYieldsEmptyChain() {
        Assert.assertTrue(SysUserCompositeImpl.listAncestorDepts(404L, DeptTreeFixture.fourLevelTree()).isEmpty());
    }

    @Test
    public void nullDeptIdYieldsEmptyChain() {
        Assert.assertTrue(SysUserCompositeImpl.listAncestorDepts(null, DeptTreeFixture.fourLevelTree()).isEmpty());
    }

    @Test
    public void brokenParentReferenceStopsTheWalk() {
        Map<Long, SysDept> tree = new HashMap<>();
        tree.put(5L, DeptTreeFixture.dept(5L, "父级不存在", 404L));
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(5L, tree::get);
        Assert.assertTrue("parent_id 指向不存在的部门时在断点结束，不抛异常", chain.isEmpty());
    }

    @Test
    public void selfReferencingDeptTerminates() {
        Map<Long, SysDept> tree = new HashMap<>();
        tree.put(9L, DeptTreeFixture.dept(9L, "自己当自己的父级", 9L));
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(9L, tree::get);
        Assert.assertTrue("自引用不得把本部门算进自己的上级", chain.isEmpty());
    }

    @Test
    public void mutualParentCycleTerminatesWithDeterministicResult() {
        Map<Long, SysDept> tree = new HashMap<>();
        tree.put(7L, DeptTreeFixture.dept(7L, "A", 8L));
        tree.put(8L, DeptTreeFixture.dept(8L, "B", 7L));
        List<ParentDept> fromA = SysUserCompositeImpl.listAncestorDepts(7L, tree::get);
        Assert.assertEquals(Collections.singletonList(8L), idsOf(fromA));
        List<ParentDept> fromB = SysUserCompositeImpl.listAncestorDepts(8L, tree::get);
        Assert.assertEquals(Collections.singletonList(7L), idsOf(fromB));
    }

    @Test
    public void cycleQueryCountIsBoundedByDeptNumber() {
        // 5 个部门首尾相连：本部门查一次，环上其余 4 个各查一次后靠 visited 终止，不会回头重查
        Map<Long, SysDept> ring = new HashMap<>();
        for (long id = 1; id <= 5; id++) {
            ring.put(id, DeptTreeFixture.dept(id, "环" + id, id == 5 ? 1L : id + 1));
        }
        AtomicInteger queries = new AtomicInteger();
        Function<Long, SysDept> counting = id -> {
            queries.incrementAndGet();
            return ring.get(id);
        };
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(1L, counting);
        Assert.assertEquals("环上另外 4 个部门都算上级，本部门不算",
                Arrays.asList(5L, 4L, 3L, 2L), idsOf(chain));
        Assert.assertEquals("查询次数被部门个数封顶地终止", 5, queries.get());
    }

    @Test
    public void deepChainKeepsEveryAncestorOnce() {
        Map<Long, SysDept> tree = new HashMap<>();
        tree.put(1L, DeptTreeFixture.dept(1L, "根", 0L));
        for (long id = 2; id <= 20; id++) {
            tree.put(id, DeptTreeFixture.dept(id, "层" + id, id - 1));
        }
        List<ParentDept> chain = SysUserCompositeImpl.listAncestorDepts(20L, tree::get);
        Assert.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L), idsOf(chain));
        List<Integer> levels = levelsOf(chain);
        Assert.assertEquals(Integer.valueOf(19), levels.get(0));
        Assert.assertEquals(Integer.valueOf(1), levels.get(levels.size() - 1));
    }
}
