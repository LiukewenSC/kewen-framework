package com.kewen.framework.common.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RegUtilTest {

    @Test
    public void extractMarkdownImages() {
        String image = "fasdgfdgadsfasfdsf![0a4d60ba14702dcb2be3aab3c7407222](http://storage.blog.liukewen.cn/kewen/notes/20230713/0a4d60ba14702dcb2be3aab3c7407222.png)agadsfasdfasdf,![0a4d60ba14702dcb2be3aab3c7407222](http://storage.blog.liukewen.cn/kewen/notes/20230713/0a4d60ba14702dcb2be3aab3c7407222.png)";
        List<RegUtil.MarkdownImage> markdownImages = RegUtil.extractMarkdownImages(image);
        assertEquals(2, markdownImages.size());
        for (RegUtil.MarkdownImage markdownImage : markdownImages) {
            assertEquals("![0a4d60ba14702dcb2be3aab3c7407222](http://storage.blog.liukewen.cn/kewen/notes/20230713/0a4d60ba14702dcb2be3aab3c7407222.png)", markdownImage.getOriginal());
            assertEquals("0a4d60ba14702dcb2be3aab3c7407222", markdownImage.getName());
            assertEquals("http://storage.blog.liukewen.cn/kewen/notes/20230713/0a4d60ba14702dcb2be3aab3c7407222.png", markdownImage.getUrl());
        }
    }
}