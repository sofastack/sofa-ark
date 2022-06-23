package com.alipay.sofa.ark.spi.model;

import org.junit.Assert;
import org.junit.Test;

public class BizOperationTest {
    @Test
    public void testOperationEqual(){
        BizOperation op1 = BizOperation.createBizOperation();
        op1.setBizName("biz A").setBizVersion("1.0.0").setOperationType(BizOperation.OperationType.INSTALL);
        BizOperation op2 = BizOperation.createBizOperation();
        op2.setBizName("biz A").setBizVersion("1.0.0").setOperationType(BizOperation.OperationType.INSTALL);

        Assert.assertEquals(op1, op1);
        Assert.assertEquals(op1, op2);
        op2.setOperationType(BizOperation.OperationType.UNINSTALL);
        Assert.assertNotEquals(op1, op2);
        op2.setBizVersion("2.0.0");
        Assert.assertNotEquals(op1,op2);
        op2.setBizName("biz B");
        Assert.assertNotEquals(op1,op2);

        Assert.assertFalse(op1.equals(""));
        Assert.assertFalse(op1.equals(null));



    }
}
