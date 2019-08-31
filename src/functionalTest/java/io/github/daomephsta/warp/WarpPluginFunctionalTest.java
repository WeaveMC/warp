/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.daomephsta.warp;

import java.io.IOException;
import java.nio.file.*;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

public class WarpPluginFunctionalTest 
{
    @Test 
    public void canRunTask() throws IOException 
    {
        // Setup the test build
        Path projectDir = Paths.get("build/functionalTest");
        Files.createDirectories(projectDir);
        Files.copy(this.getClass().getResourceAsStream("/build.gradle"), projectDir.resolve("build.gradle"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(this.getClass().getResourceAsStream("/settings.gradle"), projectDir.resolve("settings.gradle"), StandardCopyOption.REPLACE_EXISTING);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withDebug(true);
        runner.withArguments("genSources");
        runner.withProjectDir(projectDir.toFile());
        
        runner.build();
    }
} 