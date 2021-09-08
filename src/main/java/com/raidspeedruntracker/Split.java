package com.raidspeedruntracker;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class Split
{
	Duration pbDuration;
	transient Duration timeDifference;
	transient Duration originalPbDuration = this.pbDuration;
	transient Duration newPbDuration;

	transient String pbString;
	transient String splitString;

	RaidRoom raidRoom;
	transient Color splitColor = Color.WHITE;

	public Split(RaidRoom raidRoom)
	{
		this.raidRoom = raidRoom;
	}

	public String getPbString()
	{
		if (this.pbString == null && this.pbDuration != null)
		{
			this.pbString = getFormattedTimeString(pbDuration);
		}

		if (this.pbDuration == null)
		{
			return "-";
		}

		return this.pbString;
	}

	public String getSplitString()
	{
		if (this.splitString == null && this.timeDifference != null)
		{
			//Show either - or + time
			this.splitString = this.timeDifference.isNegative() ? "-" : "+";
			this.splitString += getFormattedTimeString(this.timeDifference.abs());
		}

		return this.splitString;
	}

	private String getFormattedTimeString(Duration duration)
	{
		LocalTime time = LocalTime.ofSecondOfDay(duration.getSeconds());
		String formattedTime = time.format(time.getMinute() >= 60
			? DateTimeFormatter.ofPattern("hh:mm")
			: DateTimeFormatter.ofPattern("mm:ss"));

		return formattedTime;
	}
}
