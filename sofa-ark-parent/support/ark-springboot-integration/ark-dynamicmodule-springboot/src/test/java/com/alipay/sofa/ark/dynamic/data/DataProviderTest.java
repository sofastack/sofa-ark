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
package com.alipay.sofa.ark.dynamic.data;

import com.alipay.sofa.ark.dynamic.BaseTest;
import com.alipay.sofa.ark.dynamic.support.testng.data.IDataProviderFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author hanyue
 * @version : DataProviderTest.java, v 0.1 2022年06月02日 2:16 PM hanyue Exp $
 */
public class DataProviderTest extends BaseTest {

    @DataProvider(name = "DataProvider")
    public Object[][] DataProviders() {
        Object data = IDataProviderFactory.create(new TestObjectProvider());
        Object[][] obj = new Object[][] { { "A", data } };
        return obj;
    }

    @Test(dataProvider = "DataProvider")
    public void DataProviderShow(String a, TestObject testObject) {
        System.out.println("result：" + a);
    }
}