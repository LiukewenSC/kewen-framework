package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.advance.authrange.AuthRange;
import com.kewen.framework.boot.authority.biz.mapper.TestMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author kewen
 * @descrpition 测试service
 * @since 2022-11-26 20:52
 */
@Component
public class TestService {

    @Autowired
    private TestMapper testMapper;

    @AuthRange(module = "test",tableAlias = "test")
    public List<Test> test() {
        List<Test> tests = testMapper.selectList(null);
        return tests;
    }

}
