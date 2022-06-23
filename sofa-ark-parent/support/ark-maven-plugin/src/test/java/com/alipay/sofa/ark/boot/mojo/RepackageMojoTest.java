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
package com.alipay.sofa.ark.boot.mojo;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedHashSet;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2020/12/16 2:25 下午
 * @since
 **/
public class RepackageMojoTest {

    @Test
    public void testRepackageMojo() throws NoSuchMethodException, InvocationTargetException,
                                   IllegalAccessException, NoSuchFieldException {
        RepackageMojo repackageMojo = new RepackageMojo();
        Method extensionExcludeArtifacts = repackageMojo.getClass().getDeclaredMethod(
            "extensionExcludeArtifacts", String.class);

        extensionExcludeArtifacts.setAccessible(true);
        URL resource = this.getClass().getClassLoader().getResource("excludes.txt");
        extensionExcludeArtifacts.invoke(repackageMojo, resource.getPath());
        Field excludes = repackageMojo.getClass().getDeclaredField("excludes");
        Field excludeGroupIds = repackageMojo.getClass().getDeclaredField("excludeGroupIds");
        Field excludeArtifactIds = repackageMojo.getClass().getDeclaredField("excludeArtifactIds");

        excludes.setAccessible(true);
        excludeGroupIds.setAccessible(true);
        excludeArtifactIds.setAccessible(true);

        Object excludesResult = excludes.get(repackageMojo);
        Object excludeGroupIdResult = excludeGroupIds.get(repackageMojo);
        Object excludeArtifactIdsResult = excludeArtifactIds.get(repackageMojo);
        Assert.assertTrue(excludesResult instanceof LinkedHashSet
                          && excludeGroupIdResult instanceof LinkedHashSet
                          && excludeArtifactIdsResult instanceof LinkedHashSet);
        Assert.assertTrue(((LinkedHashSet) excludesResult).contains("tracer-core:3.0.10")
                          && ((LinkedHashSet) excludesResult).contains("tracer-core:3.0.11"));
    }
}
