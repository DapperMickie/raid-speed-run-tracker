package com.raidspeedruntracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raidspeedruntracker")
public interface RaidSpeedRunTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
		keyName = "trackMilliseconds",
		name = "Track Milliseconds",
		description = "Track splits with millisecond accuracy"
	)
	default boolean trackMilliseconds(){
		return true;
	}

	@ConfigItem(
		keyName = "numLines",
		name = "Number of lines",
		description = "Number of lines to show in the split tracker overlay"
	)
	default int numLines(){
		return 3;
	}
}
