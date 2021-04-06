package io.github.daomephsta.warp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.gradle.api.*;
import org.gradle.api.tasks.TaskContainer;

import io.github.daomephsta.warp.configuration.WarpConfigurations;
import io.github.daomephsta.warp.extension.WarpExtension;
import io.github.daomephsta.warp.task.WarpTasks;
import net.fabricmc.loom.LoomGradleExtension;

public class WarpPlugin implements Plugin<Project> 
{
    public void apply(Project project) 
    {
        WarpConfigurations.register(project.getConfigurations());
        WarpTasks.register(project.getTasks());
        project.getExtensions().create("warp", WarpExtension.class);
        project.afterEvaluate(this::afterProjectEvaluation);
    }
    
    private void afterProjectEvaluation(Project project)
    {
    	TaskContainer tasks = project.getTasks();
		Task genSources = tasks.getByName("genSources");
		
		genSources.dependsOn(WarpTasks.REMAP_NAMED_MC_JAR);
		genSources.doLast(t -> 
		{
			try
			{
				//Replace mapped JAR with pre-unpicking mapped JAR made earlier, so @ModifyConstant mixins aren't broken 
				LoomGradleExtension loomExtension = t.getProject().getExtensions().getByType(LoomGradleExtension.class);
				File mappedJar = loomExtension.getMinecraftMappedProvider().getMappedJar();
				File inlinedMappedJar = new File(t.getProject().getBuildDir(), "tmp/warp/inlined/" + mappedJar.getName());
				mappedJar.delete();
				FileUtils.copyFile(inlinedMappedJar, mappedJar);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});
    }
    
	public static Path getUnpickConfigurationCache(Project project)
	{
		return project.getGradle().getGradleUserHomeDir().toPath().resolve("caches/warp/unpick-configuration");
	}
}
