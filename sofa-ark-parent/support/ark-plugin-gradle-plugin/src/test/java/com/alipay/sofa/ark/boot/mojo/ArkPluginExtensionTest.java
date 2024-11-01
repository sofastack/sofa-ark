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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class ArkPluginExtensionTest {

    private Project project;
    private ArkPluginExtension extension;

    @Before
    public void setup() {
        project = ProjectBuilder.builder().withName("test-project").build();
        project.getPluginManager().apply("sofa-ark-plugin-gradle-plugin");
        extension = project.getExtensions().getByType(ArkPluginExtension.class);
    }

    @Test
    public void testDefaultValues() {
        assertEquals("100", extension.getPriority().get());
        assertEquals("test-project", extension.getPluginName().get());
        assertEquals("", extension.getDescription().get());
        assertEquals("", extension.getActivator().get());
        assertTrue(extension.getOutputDirectory().get().getAsFile().getPath().endsWith("build" + File.separator + "libs"));
    }

    @Test
    public void testSetAndGetValues() {
        extension.getPriority().set("200");
        extension.getPluginName().set("test-plugin");
        extension.getDescription().set("Test description");
        extension.getActivator().set("com.example.TestActivator");
        extension.getAttach().set(true);

        assertEquals("200", extension.getPriority().get());
        assertEquals("test-plugin", extension.getPluginName().get());
        assertEquals("Test description", extension.getDescription().get());
        assertEquals("com.example.TestActivator", extension.getActivator().get());
        assertTrue(extension.getAttach().get());
    }

}
