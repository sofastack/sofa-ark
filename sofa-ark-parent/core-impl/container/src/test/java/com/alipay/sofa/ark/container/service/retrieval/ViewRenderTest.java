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
package com.alipay.sofa.ark.container.service.retrieval;

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.session.handler.ArkCommandHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yanzhu
 * @date 2023/10/17 17:55
 */
public class ViewRenderTest extends BaseTest {

    @Test
    public void testRenderClassInfo() {

        ClassInfoVO classInfo = new ClassInfoVO();
        classInfo.setClassInfo("com.example.HelloWorld");
        classInfo.setCodeSource("/home/admin");
        classInfo.setInterface(false);
        classInfo.setAnnotation(false);
        classInfo.setEnum(false);
        classInfo.setContainerName("A1:V1");
        classInfo.setSimpleName("helloWorld");
        classInfo.setModifier("public");
        classInfo.setSuperClass(new String[] { "com.example" });
        classInfo.setClassloader(new String[] { "com.example.ClassLoader" });

        String renderClassInfo = ViewRender.renderClassInfo(classInfo);

        Assert.assertTrue(renderClassInfo.contains("class-info"));
        Assert.assertTrue(renderClassInfo.contains("code-source"));
        Assert.assertTrue(renderClassInfo.contains("isInterface"));
        Assert.assertTrue(renderClassInfo.contains("isAnnotation"));
        Assert.assertTrue(renderClassInfo.contains("isEnum"));
        Assert.assertTrue(renderClassInfo.contains("container-name"));
        Assert.assertTrue(renderClassInfo.contains("simple-name"));
        Assert.assertTrue(renderClassInfo.contains("modifier"));
        Assert.assertTrue(renderClassInfo.contains("super-class"));
        Assert.assertTrue(renderClassInfo.contains("class-loader"));

    }

}
