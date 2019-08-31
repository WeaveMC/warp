package io.github.daomephsta.warp.task;

import org.gradle.api.tasks.TaskContainer;

public class WarpTasks
{
	public static final String UNZIP_UNPICK_CONFIGURATION = "unzipUnpickConfiguration";
	public static final String CONFIGURE_WARP = "configureWarp";
	public static final String REMAP_UNPICK_DEFINITIONS = "remapUnpickDefinitions";
	public static final String REMAP_NAMED_MC_JAR = "remapNamedMCJar";

	public static void register(TaskContainer tasks)
	{
		tasks.register(UNZIP_UNPICK_CONFIGURATION, UnzipUnpickConfigurationTask.class);
		tasks.register(CONFIGURE_WARP, ConfigureWarpTask.class, task -> 
		{
			task.dependsOn(UNZIP_UNPICK_CONFIGURATION);
		});
		tasks.register(REMAP_UNPICK_DEFINITIONS, RemapUnpickDefinitionsTask.class, task ->
		{
			task.dependsOn(CONFIGURE_WARP);
		});
		tasks.register(REMAP_NAMED_MC_JAR, RemapNamedMinecraftJarTask.class, task ->
		{
			task.dependsOn(REMAP_UNPICK_DEFINITIONS);
		});
	}
}
