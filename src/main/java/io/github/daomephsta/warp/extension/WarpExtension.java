package io.github.daomephsta.warp.extension;

import java.nio.file.Path;

import org.gradle.api.Project;

public class WarpExtension
{
	private Path unpickDefinitions = null;

	public static WarpExtension get(Project project)
	{
		return project.getExtensions().getByType(WarpExtension.class);
	}

	public Path getUnpickDefinitionsLocation()
	{
		return unpickDefinitions;
	}

	public void setUnpickDefinitionsLocation(Path unpickDefinitions)
	{
		this.unpickDefinitions = unpickDefinitions;	
	}
}
