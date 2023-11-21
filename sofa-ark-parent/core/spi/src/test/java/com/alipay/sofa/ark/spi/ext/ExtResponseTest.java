/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        assertEquals("ExtResponse{" + "success=true, errorMsg='myMsg'" + ", errorCode='001'"
                     + ", data=myData}", extResponseStr);
    }
}
