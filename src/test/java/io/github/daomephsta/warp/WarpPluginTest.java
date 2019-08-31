package io.github.daomephsta.warp;

import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class WarpPluginTest 
{
    @Test 
    public void pluginRegistersATask() 
    {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("io.github.daomephsta.warp");
        project.getPlugins().apply("fabric-loom");

        System.out.println(project.getTasks().getByName("genSources"));
        assertTrue(project.getTasks().getByName("genSources") != null);
    }
}
