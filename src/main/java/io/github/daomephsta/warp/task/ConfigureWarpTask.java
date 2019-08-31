package io.github.daomephsta.warp.task;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.*;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.github.daomephsta.warp.configuration.WarpConfigurations;
import io.github.daomephsta.warp.extension.WarpExtension;

public class ConfigureWarpTask extends DefaultTask
{
	private final SAXParser parser;

	public ConfigureWarpTask()
	{
		try
		{
			this.parser = SAXParserFactory.newInstance().newSAXParser();
		} 
		catch (ParserConfigurationException | SAXException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@TaskAction
	private void loadConfig()
	{
		Path warpConfig = WarpExtension.get(getProject()).getUnpickDefinitionsLocation().resolve("warp-config.xml");
		try
		{
			parser.parse(warpConfig.toFile(), new WarpConfigurator(getProject()));
		} 
		catch (SAXException | IOException e)
		{
			e.printStackTrace();
		}
	}
	

	public static class WarpConfigurator extends DefaultHandler
	{
		private final Project project;
		
		private WarpConfigurator(Project project)
		{
			this.project = project;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			switch (qName)
			{
			case "constantSource" :
				project.getDependencies().add(WarpConfigurations.UNPICK_CONSTANT_SOURCE, attributes.getValue("mavenStyleId"));
				break;

			default :
				break;
			}
		}
	}
}
