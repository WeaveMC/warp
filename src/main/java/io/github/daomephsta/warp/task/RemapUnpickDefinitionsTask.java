package io.github.daomephsta.warp.task;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import daomephsta.unpick.constantmappers.datadriven.parser.MethodKey;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.*;
import io.github.daomephsta.warp.WarpPlugin;
import io.github.daomephsta.warp.extension.WarpExtension;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.providers.MappingsProvider;
import net.fabricmc.mapping.tree.*;

public class RemapUnpickDefinitionsTask extends DefaultTask
{	
	private static final String OBF_NAMESPACE = "official";
	private static final String HUMAN_NAMESPACE = "named";

	@TaskAction
	public void doTask() throws IOException 
	{
		LoomGradleExtension loomExtension = getProject().getExtensions().getByType(LoomGradleExtension.class);
		MappingsProvider mappingsProvider = loomExtension.getMappingsProvider();
		
		Path unpickDefinitionsLocation = WarpExtension.get(getProject()).getUnpickDefinitionsLocation();
		String baseName = unpickDefinitionsLocation.getFileName().toString() 
				+ "_" + mappingsProvider.mappingsName + "-" + mappingsProvider.minecraftVersion 
				+ "+build." + mappingsProvider.mappingsVersion;
		Path destination = WarpPlugin.getUnpickConfigurationCache(getProject()).resolve(baseName);
		WarpExtension.get(getProject()).setUnpickDefinitionsLocation(destination);
		if (Files.exists(destination))
			return;
		
		TinyTree mappings = getMappings(mappingsProvider);
		Map<String, String> classMappings = new HashMap<>();
		Map<MethodKey, String> methodMappings = new HashMap<>();
		for (ClassDef classDef : mappings.getClasses())
		{
			String officialClassName = classDef.getName(OBF_NAMESPACE);
			classMappings.put(officialClassName, classDef.getName(HUMAN_NAMESPACE));
			for (MethodDef methodDef : classDef.getMethods())
			{
				MethodKey key = new MethodKey(officialClassName, methodDef.getName(OBF_NAMESPACE), methodDef.getDescriptor(OBF_NAMESPACE));
				methodMappings.put(key, methodDef.getName(HUMAN_NAMESPACE));
			}
		}
		Files.createDirectories(destination);
		
		//Generate remapped files
		for (File file : unpickDefinitionsLocation.toFile().listFiles())
		{
			if (!FilenameUtils.getExtension(file.getName()).equals("unpick"))
				continue;
			InputStream stream = new FileInputStream(file);
			try(
					UnpickV2Reader unpickReader = new UnpickV2Reader(stream);
					Writer out = Files.newBufferedWriter(destination.resolve(file.getName()), StandardOpenOption.CREATE);
			)
			{
				UnpickV2Writer writer = new UnpickV2Writer();
				unpickReader.accept(new UnpickV2Remapper(classMappings, methodMappings, writer));
				out.write(writer.getOutput());
			}
		}
	}
	
	private static TinyTree getMappings(MappingsProvider mappingsProvider)
	{
		try
		{
			return mappingsProvider.getMappings();
		} 
		catch (IOException e)
		{
			throw new RuntimeException("Failed to get local mappings", e);
		}
	}
}
