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
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("sofa.ark.plugin.gradle.plugin");
        extension = project.getExtensions().getByType(ArkPluginExtension.class);
    }

    @Test
    public void testDefaultValues() {
        assertEquals("100", extension.getPriority().get());
        assertEquals("sofa-ark-plugin-gradle-plugin", extension.getPluginName().get());
        assertEquals("ark plugin", extension.getDescription().get());
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
