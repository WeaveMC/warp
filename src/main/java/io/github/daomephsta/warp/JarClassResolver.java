package io.github.daomephsta.warp;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.StreamSupport;

import org.objectweb.asm.ClassReader;

import daomephsta.unpick.api.IClassResolver;

public class JarClassResolver implements IClassResolver
{
	private final Map<String, ClassReaderSupplier> classReaderSuppliers = new HashMap<>(); 
	private final Iterable<File> jars;

	public JarClassResolver(Iterable<File> jars)
	{
		this.jars = StreamSupport.stream(jars.spliterator(), false).collect(toSet());
	}
	
	@Override
	public ClassReader resolveClass(String internalName) throws IOException
	{
		ClassReaderSupplier classSupplier = classReaderSuppliers.get(internalName);
		return classSupplier != null 
			? classSupplier.get()
			: findClass(internalName);
	}
	
	public ClassReader findClass(String internalName)
	{
		ClassReader classReader = null;
		for (File jar : jars)
		{
			try(JarFile jarFile = new JarFile(jar))
			{
				String classEntryName = internalName.replace('.', '/') + ".class";
				JarEntry classEntry = jarFile.getJarEntry(classEntryName);
				if (classEntry != null)
				{
					classReaderSuppliers.put(internalName, new ClassReaderSupplier(jar, classEntryName));
					classReader = new ClassReader(jarFile.getInputStream(classEntry));
				}
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		if (classReader == null)
			throw new RuntimeException(internalName + " not found in any of " + jars);
		return classReader;
	}
	
	private static class ClassReaderSupplier
	{
		private final File parentJar;
		private final String entryName;
		
		public ClassReaderSupplier(File parentJar, String entryName)
		{
			this.parentJar = parentJar;
			this.entryName = entryName;
		}

		public ClassReader get()
		{
			try(JarFile jarFile = new JarFile(parentJar))
			{
				return new ClassReader(jarFile.getInputStream(jarFile.getJarEntry(entryName)));
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
