package com.cu.framework.basic.exception;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * 错误分类的固定映射与数据库异常识别
 *
 * @author kewen
 * @since 2026-10-30
 */
public class ErrorTypeTest {

    @Test
    public void statusMatchesFixedMapping() {
        Assert.assertEquals(401, ErrorType.AUTHENTICATION.status());
        Assert.assertEquals(403, ErrorType.AUTHORIZATION.status());
        Assert.assertEquals(400, ErrorType.VALIDATION.status());
        Assert.assertEquals(400, ErrorType.BUSINESS.status());
        Assert.assertEquals(400, ErrorType.USAGE.status());
        Assert.assertEquals(500, ErrorType.DATABASE.status());
        Assert.assertEquals(500, ErrorType.SYSTEM.status());
    }

    @Test
    public void onlyDatabaseAndSystemAreServerFaults() {
        Assert.assertTrue(ErrorType.DATABASE.serverFault());
        Assert.assertTrue(ErrorType.SYSTEM.serverFault());
        Assert.assertFalse(ErrorType.AUTHENTICATION.serverFault());
        Assert.assertFalse(ErrorType.AUTHORIZATION.serverFault());
        Assert.assertFalse(ErrorType.VALIDATION.serverFault());
        Assert.assertFalse(ErrorType.BUSINESS.serverFault());
        Assert.assertFalse(ErrorType.USAGE.serverFault());
    }

    @Test
    public void detectsDatabaseFailureInCauseChain() {
        DataAccessResourceFailureException database = new DataAccessResourceFailureException("db down");
        Assert.assertTrue(ErrorType.causedByDatabase(database));
        Assert.assertTrue(ErrorType.causedByDatabase(new IllegalStateException("wrapped", database)));
        Assert.assertFalse(ErrorType.causedByDatabase(new IllegalStateException("not a database failure")));
        Assert.assertFalse(ErrorType.causedByDatabase(null));
    }
}
