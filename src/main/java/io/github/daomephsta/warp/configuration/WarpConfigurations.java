package io.github.daomephsta.warp.configuration;

import org.gradle.api.artifacts.ConfigurationContainer;

public class WarpConfigurations
{
	public static final String UNPICK_CONSTANT_SOURCE = "unpickConstantSource",
							   UNPICK_CONFIGURATION = "unpickDefinitions";

	public static void register(ConfigurationContainer configurations)
	{
		configurations.register(UNPICK_CONFIGURATION);
		configurations.register(UNPICK_CONSTANT_SOURCE);
	}
}
