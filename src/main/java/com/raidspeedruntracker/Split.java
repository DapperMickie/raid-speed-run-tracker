package com.raidspeedruntracker;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.runelite.client.plugins.raids.RaidRoom;

public class Split
{
	transient Instant startTime;

	Duration pbDuration;
	transient Duration splitDuration;
	transient Duration originalPbDuration;
	transient Duration s1;
	transient Duration timeDiff;

	transient String pbString;
	transient String splitString;

	RaidRoom raidRoom;
	transient Color splitColor = Color.WHITE;

	public Split(Instant startTime, Duration pbDuration, Duration splitDuration, RaidRoom raidRoom)
	{
		this.startTime = startTime;
		this.pbDuration = pbDuration;
		this.splitDuration = splitDuration;
		this.raidRoom = raidRoom;

		if (pbDuration != null)
		{
			LocalTime pbTime = LocalTime.ofSecondOfDay(pbDuration.getSeconds());
			String formattedPbTime = pbTime.format(DateTimeFormatter.ofPattern("mm:ss"));
			pbString = formattedPbTime;
		}
		else
		{
			pbString = "-";
		}
	}

	public void InitSplit()
	{
		if (pbDuration != null)
		{
			LocalTime pbTime = LocalTime.ofSecondOfDay(pbDuration.getSeconds());
			String formattedPbTime = pbTime.format(DateTimeFormatter.ofPattern("mm:ss"));
			pbString = formattedPbTime;
			this.originalPbDuration = pbDuration;
		}
		else
		{
			pbString = "-";
		}
	}
}
