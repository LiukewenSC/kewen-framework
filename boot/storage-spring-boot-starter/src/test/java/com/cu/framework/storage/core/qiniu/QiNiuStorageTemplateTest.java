package com.cu.framework.storage.core.qiniu;

import com.cu.framework.storage.core.model.PreUploadTokenBO;
import com.qiniu.storage.Region;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @descrpition 七牛对象 key 的生成规则与预上传凭证策略
 * @author kewen
 * @since 2026-10-13
 */
public class QiNiuStorageTemplateTest {

    private static final String CALLBACK = "https://api.example.com/storage/upload/callback";

    private QiNiuStorageTemplate template(String rootPath, String persistentNotifyUrl) {
        return new QiNiuStorageTemplate("testAccessKey", "testSecretKey", Region.region2(), "test-bucket",
                rootPath, true, "static.example.com", CALLBACK, persistentNotifyUrl, false);
    }

    @Test
    public void blankRootPathProducesNoLeadingSeparator() {
        Assert.assertEquals("avatar/a.png", template("", null).resolveKey("avatar", "a.png"));
        Assert.assertEquals("avatar/a.png", template(null, null).resolveKey("avatar", "a.png"));
        Assert.assertEquals("avatar/a.png", template("   ", null).resolveKey("avatar", "a.png"));
    }

    @Test
    public void blankRelativeDirectoryIsSkipped() {
        Assert.assertEquals("app/a.png", template("app", null).resolveKey("", "a.png"));
        Assert.assertEquals("app/a.png", template("app", null).resolveKey(null, "a.png"));
    }

    @Test
    public void allSegmentsPresent() {
        Assert.assertEquals("app/avatar/a.png", template("app", null).resolveKey("avatar", "a.png"));
    }

    @Test
    public void neverProducesLeadingTrailingOrDoubleSeparators() {
        String[][] inputs = {
                {"", "a.png"}, {null, "a.png"}, {"", ""}, {"app", "a.png"}
        };
        for (String rootPath : new String[]{"", null, "app", "app/", "/app", "app//", "/"}) {
            for (String[] in : inputs) {
                String key = template(rootPath, null).resolveKey(in[0], in[1]);
                Assert.assertFalse("不应以前导分隔符开头: " + key, key.startsWith("/"));
                Assert.assertFalse("不应以分隔符结尾: " + key, key.endsWith("/"));
                Assert.assertFalse("不应出现连续分隔符: " + key, key.contains("//"));
            }
        }
    }

    @Test
    public void straySeparatorsInsideSegmentsAreTrimmed() {
        Assert.assertEquals("app/avatar/a.png", template("app/", null).resolveKey("/avatar", "a.png"));
        Assert.assertEquals("avatar/a.png", template("", null).resolveKey("/avatar/", "a.png"));
        Assert.assertEquals("a.png", template("/", null).resolveKey("/", "a.png"));
    }

    @Test
    public void preUploadTokenKeyFollowsTheSameRule() {
        PreUploadTokenBO bo = template("app", null).createPreUploadToken("avatar", "a.png");
        Assert.assertEquals("app/avatar/a.png", bo.getKey());

        PreUploadTokenBO noRoot = template("", null).createPreUploadToken("avatar", "a.png");
        Assert.assertEquals("avatar/a.png", noRoot.getKey());
    }

    @Test
    public void notifyUrlPolicyFollowsConfiguration() {
        String without = policyOf(template("app", null).createPreUploadToken("avatar", "a.png"));
        Assert.assertFalse("未配置时不应写入 persistentNotifyUrl", without.contains("persistentNotifyUrl"));

        String with = policyOf(template("app", "https://api.example.com/notify").createPreUploadToken("avatar", "a.png"));
        Assert.assertTrue("配置后应写入 persistentNotifyUrl", with.contains("persistentNotifyUrl"));
    }

    @Test
    public void callbackUrlIsAlwaysInPolicy() {
        String policy = policyOf(template("app", null).createPreUploadToken("avatar", "a.png"));
        Assert.assertTrue(policy.contains("callbackUrl"));
        Assert.assertTrue(policy.contains(CALLBACK));
    }

    /**
     * 上传凭证格式为 accessKey:sign:base64(policy)，解出第三段的 putPolicy JSON 以便断言策略内容
     */
    private String policyOf(PreUploadTokenBO bo) {
        Assert.assertNotNull(bo.getUploadToken());
        String[] parts = bo.getUploadToken().split(":");
        Assert.assertEquals("凭证应为 ak:sign:encodedPolicy 三段", 3, parts.length);

        String encoded = parts[2].replace('-', '+').replace('_', '/');
        switch (encoded.length() % 4) {
            case 2: encoded += "=="; break;
            case 3: encoded += "="; break;
            default: break;
        }
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
