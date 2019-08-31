package io.github.daomephsta.warp.task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.*;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import daomephsta.unpick.api.ConstantUninliner;
import daomephsta.unpick.api.IClassResolver;
import daomephsta.unpick.api.constantmappers.ConstantMappers;
import daomephsta.unpick.api.constantresolvers.ConstantResolvers;
import io.github.daomephsta.warp.JarClassResolver;
import io.github.daomephsta.warp.configuration.WarpConfigurations;
import io.github.daomephsta.warp.extension.WarpExtension;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.util.Constants;

public class RemapNamedMinecraftJarTask extends DefaultTask
{
	public RemapNamedMinecraftJarTask() {}
	
	@TaskAction
	private void remapJar()
	{
		try
		{
			WarpExtension warpExtension = WarpExtension.get(getProject());
			LoomGradleExtension loomExtension = getProject().getExtensions().getByType(LoomGradleExtension.class);
			Path unpickDefinitionsLocation = warpExtension.getUnpickDefinitionsLocation();
			InputStream[] unpickDefinitions = Files.walk(unpickDefinitionsLocation)
				.filter(p -> p.toString().endsWith(".unpick"))
				.map(this::openPath)
				.toArray(InputStream[]::new);
			File logFile = new File(getProject().getBuildDir(), "logs/warp/unpick.log");
			logFile.getParentFile().mkdirs();
			Iterable<File> methodOwnerSources = getProject().getConfigurations().getByName(Constants.MINECRAFT_DEPENDENCIES)
					.plus(getProject().files(loomExtension.getMinecraftMappedProvider().getMappedJar()))
					.plus(getProject().fileTree(System.getProperty("java.home") + "/lib").filter(f -> f.getName().endsWith(".jar")));
			IClassResolver methodOwnerResolver = new JarClassResolver(methodOwnerSources);
			IClassResolver constantClassResolver = new JarClassResolver(getProject().getConfigurations().getByName(WarpConfigurations.UNPICK_CONSTANT_SOURCE));
			ConstantUninliner uninliner = new ConstantUninliner(ConstantMappers.dataDriven(methodOwnerResolver, unpickDefinitions), 
					ConstantResolvers.bytecodeAnalysis(constantClassResolver), 
					logFile.getPath());
			File mappedJar = loomExtension.getMinecraftMappedProvider().getMappedJar();
			//Make copy of mapped jar pre-unpicking
			File inlinedMappedJar = new File(getProject().getBuildDir(), "tmp/warp/inlined/" + mappedJar.getName());
			FileUtils.copyFile(mappedJar, inlinedMappedJar);
			//Unpick class files of mapped jar, outputting to a temporary jar 
			File unpickedJar = new File(getProject().getBuildDir(), "tmp/warp/" + mappedJar.getName());
			unpickedJar.getParentFile().mkdirs();
			try
			(
				JarFile jar = new JarFile(mappedJar);
				JarOutputStream out = new JarOutputStream(new FileOutputStream(unpickedJar), jar.getManifest());
			)
			{ 
				Enumeration<JarEntry> entries = jar.entries();
				while(entries.hasMoreElements())
				{
					JarEntry entry = entries.nextElement();
					if (!entry.getName().endsWith(".class"))
						continue;
					InputStream inputStream = jar.getInputStream(entry);
					ClassReader classReader = new ClassReader(inputStream);
					ClassNode classNode = new ClassNode();
					classReader.accept(classNode, 0);
					uninliner.transform(classNode);
					ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
					classNode.accept(classWriter);
					JarEntry remappedEntry = new JarEntry(entry.getName());
					remappedEntry.setTime(System.currentTimeMillis());
					byte[] remappedBytes = classWriter.toByteArray();
					remappedEntry.setSize(remappedBytes.length);
					out.putNextEntry(remappedEntry);
					out.write(remappedBytes);
					out.closeEntry();
				}
			}
			//Replace mapped jar with temporary JAR
			if (mappedJar.delete() && unpickedJar.renameTo(mappedJar))
				unpickedJar.delete();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private InputStream openPath(Path path)
	{
		try
		{
			return Files.newInputStream(path);
		} 
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
