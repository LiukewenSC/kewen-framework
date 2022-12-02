package com.kewen.common.utils;


import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MapUtilTest {

    @Test
    public void builder() {
        Map<String, Integer> build = MapUtil.builder(String.class, Integer.class)
                .put("a", 1)
                .put("b", 2)
                .build();
        Assert.assertEquals(1, (int) build.get("a"));
    }

    private Map<String, Map> map() {
        return MapUtil.builder(String.class, Map.class)
                .put(
                        "spring",
                        MapUtil.builder(String.class, Map.class)
                                .put(
                                        "datasource",
                                        MapUtil.builder(String.class, String.class)
                                                .put("url", "jdbc:mysql//localhost:3306")
                                                .put("username", "uucs")
                                                .put("password", "uucs123456")
                                                .build()
                                ).build()

                ).build();
    }

    @Test
    public void getValue() {
        Map<String, Map> map = map();
        Datasource value = MapUtil.getValue(map, "spring.datasource", Datasource.class);
        Assert.assertEquals("uucs", value.getUsername());
    }

    @Test
    public void setValue() {
        Map<String, Map> map = map();
        MapUtil.setValue(map, "spring.datasource.name", "uucs2");
        String value = MapUtil.getValue(map, "spring.datasource.name", String.class);
        Assert.assertEquals("uucs2", value);

    }

    private static class Datasource {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}