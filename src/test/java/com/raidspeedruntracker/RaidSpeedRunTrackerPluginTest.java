package com.raidspeedruntracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RaidSpeedRunTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RaidSpeedRunTrackerPlugin.class);
		RuneLite.main(args);
	}
}