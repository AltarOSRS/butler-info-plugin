package com.butlerinfo;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ButlerInfoPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ButlerInfoPlugin.class);
		RuneLite.main(args);
	}
}