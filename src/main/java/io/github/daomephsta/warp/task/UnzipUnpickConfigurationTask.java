package io.github.daomephsta.warp.task;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;

import io.github.daomephsta.warp.WarpPlugin;
import io.github.daomephsta.warp.configuration.WarpConfigurations;
import io.github.daomephsta.warp.extension.WarpExtension;

public class UnzipUnpickConfigurationTask extends DefaultTask
{
	public UnzipUnpickConfigurationTask()
	{
		Configuration unpickConfiguration = getProject().getConfigurations().getByName(WarpConfigurations.UNPICK_CONFIGURATION);
		getInputs().files(unpickConfiguration);
		Path unpickConfigurationCache = WarpPlugin.getUnpickConfigurationCache(getProject());
		getOutputs().dir(unpickConfigurationCache);
		doLast(t -> 
		{
			if (unpickConfiguration.getDependencies().size() == 0)
				throw new IllegalStateException("Missing Unpick definitions");
			if (unpickConfiguration.getDependencies().size() > 1)
				throw new IllegalStateException("Defining multiple sets of Unpick configurations is invalid");
			File unpickDef = unpickConfiguration.getSingleFile();
			String baseName = FilenameUtils.getBaseName(unpickDef.getName());
			Path destination = unpickConfigurationCache.resolve(baseName);
			WarpExtension.get(t.getProject()).setUnpickDefinitionsLocation(destination);
			t.getProject().copy(spec -> 
			{
				spec.from(t.getProject().zipTree(unpickDef));
				spec.into(destination);
			});
		});
	}
}
