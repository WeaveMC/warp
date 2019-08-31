package io.github.daomephsta.warp.task;

import static java.util.stream.Collectors.toMap;

import java.io.*;
import java.nio.file.*;
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
import net.fabricmc.mappings.EntryTriple;
import net.fabricmc.mappings.Mappings;

public class RemapUnpickDefinitionsTask extends DefaultTask
{	
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
		
		Mappings mappings = getMappings(mappingsProvider);
		Map<String, String> classMappings = mappings.getClassEntries().stream().collect(toMap(ce -> ce.get("official"), ce -> ce.get("named")));
		Map<MethodKey, String> methodMappings = mappings.getMethodEntries().stream().collect(toMap(me -> 
		{
			EntryTriple methodInfo = me.get("official");
			return new MethodKey(methodInfo.getOwner(), methodInfo.getName(), methodInfo.getDesc());
		}, 
		me -> me.get("named").getName()));
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
	
	private static Mappings getMappings(MappingsProvider mappingsProvider)
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
