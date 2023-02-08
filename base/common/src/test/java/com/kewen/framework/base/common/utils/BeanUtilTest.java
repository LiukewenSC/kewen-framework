package com.kewen.framework.base.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.base.common.model.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BeanUtilTest {

    @Test
    public void parseJson() {

        List<TestParse> testParses = Arrays.asList(new TestParse(1, "nick1"), new TestParse(2, "nick2"));

        System.out.println(JSONObject.toJSONString(testParses));
        String string = null;

        String s = "[{\"id\":1,\"nickName\":\"nick1\"},{\"id\":2,\"nick_name\":\"nick2\"}]";

        List<TestParse> parses = BeanUtil.parseObject(s, new TypeReference<List<TestParse>>(TestParse.class) {});

        System.out.println(JSONObject.toJSONString(parses));

    }

    public static class TestParse{
        private Integer id;
        private String nickName;

        public TestParse() {
        }

        public TestParse(Integer id, String nickName) {
            this.id = id;
            this.nickName = nickName;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
    }
}