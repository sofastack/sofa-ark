package com.alipay.sofa.ark.spi.ext;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.springframework.beans.BeanUtils.copyProperties;

public class ExtResponseTest {

    @Test
    public void testAllMethods() {
        copyProperties(new ExtResponse<>(), new ExtResponse<>());
        ExtResponse<String> extResponse = new ExtResponse<>();
        extResponse.setSuccess(true);
        extResponse.setErrorMsg("myMsg");
        extResponse.setErrorCode("001");
        extResponse.setData("myData");
        String extResponseStr = extResponse.toString();
        assertEquals("ExtResponse{" + "success=true, errorMsg='myMsg'"
                + ", errorCode='001'" + ", data=myData}", extResponseStr);
    }
}
