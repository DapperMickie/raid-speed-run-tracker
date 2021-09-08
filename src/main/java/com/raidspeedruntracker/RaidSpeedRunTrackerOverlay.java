package com.raidspeedruntracker;

import com.google.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;


public class RaidSpeedRunTrackerOverlay extends Overlay
{
	private final Client client;
	private final RaidSpeedRunTrackerConfig config;
	private final PanelComponent panelComponent = new PanelComponent();
	private final RaidSpeedRunTrackerPlugin raidSpeedRunTrackerPlugin;
	private final static String HOUR_FORMAT = "HH:mm:ss";
	private final static String MINUTE_FORMAT = "mm:ss";
	private final static String MILLISECOND_FORMAT = "ss.ss";

	@Inject
	public RaidSpeedRunTrackerOverlay(Client client, RaidSpeedRunTrackerConfig config, RaidSpeedRunTrackerPlugin raidSpeedRunTrackerPlugin)
	{
		setPosition(OverlayPosition.TOP_LEFT);
		this.client = client;
		this.config = config;
		this.raidSpeedRunTrackerPlugin = raidSpeedRunTrackerPlugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		String overlayTitle = "CoX Splits";

		panelComponent.getChildren().add(TitleComponent.builder()
			.text(overlayTitle)
			.color(Color.white)
			.build());

		Instant startTime = raidSpeedRunTrackerPlugin.GetStartTime();
		RaidRoom currentRoom = raidSpeedRunTrackerPlugin.getCurrentRoom();
		boolean isRaidComplete = raidSpeedRunTrackerPlugin.IsRaidComplete();

		LocalTime time;
		Duration elapsed;

		Color timeColor = Color.WHITE;

		Split[] splits = raidSpeedRunTrackerPlugin.getSplits();

		//Figure out how many splits to show at a time
		int offset = config.numLines() / 2;
		int startRoomIndex = currentRoom.getPosition();

		if (startRoomIndex + config.numLines() > splits.length + 1)
		{
			startRoomIndex = splits.length - config.numLines();
		}
		else if (startRoomIndex - offset < 0)
		{
			startRoomIndex = 0;
		}
		else
		{
			startRoomIndex = startRoomIndex - offset;
		}

		//Display splits
		for (int i = startRoomIndex; i < config.numLines() + startRoomIndex; i++)
		{
			Split split = splits[i];

			String splitString = "";
			Color splitColor = Color.WHITE;
			//If the raid is complete, dont use current row
			boolean currentRow = i == currentRoom.getPosition() && !isRaidComplete;

			//If it's the current row, show the time difference between now and the pb split
			//Also, determine the color based on the time difference
			if (currentRow)
			{
				if (split.timeDifference == null
					&& split.pbDuration != null)
				{
					Duration splitDuration = Duration.between(startTime, Instant.now());
					splitDuration = splitDuration.minus(split.pbDuration);

					LocalTime splitTime = LocalTime.ofSecondOfDay(splitDuration.abs().getSeconds());

					if (splitDuration.isNegative())
					{
						splitColor = Color.GREEN;
						splitString += "-";
						if (!isRaidComplete)
						{
							timeColor = Color.GREEN;
						}
					}
					else
					{
						splitColor = Color.RED;
						splitString += "+";
						if (!isRaidComplete)
						{
							timeColor = Color.RED;
						}
					}

					DateTimeFormatter splitTimeFormat = getTimeFormat(splitTime);
					splitString += splitTime.format(splitTimeFormat);
				}
			}
			else
			{
				//If the split has already been calculated, display it
				if (split.getSplitString() != null)
				{
					splitString = split.getSplitString();
					splitColor = split.getSplitColor();
				}
			}

			//Denote current split with arrow
			panelComponent.getChildren().add(LineComponent.builder()
				.left(split.raidRoom.getName() + (currentRow ? " <-" : ""))
				.right(splitString + " " + split.getPbString())
				.rightColor(splitColor)
				.build());
		}

		//Keep track of elapsed time
		//If raid is complete, stop the timer and don't keep updating it
		if (raidSpeedRunTrackerPlugin.IsRaidComplete())
		{
			Split endSplit = raidSpeedRunTrackerPlugin.getSplit(RaidRoom.OLM);
			elapsed = endSplit.getNewPbDuration();
			Duration originalPbDuration = endSplit.originalPbDuration;

			if (originalPbDuration != null)
			{
				if (elapsed.compareTo(originalPbDuration) < 0)
				{
					timeColor = Color.GREEN;
				}
				else
				{
					timeColor = Color.RED;
				}
			}
		}
		else
		{
			elapsed = Duration.between(startTime, Instant.now());
		}

		time = LocalTime.ofSecondOfDay(elapsed.getSeconds());
		DateTimeFormatter timeFormat = getTimeFormat(time);
		String formattedTime = time.format(timeFormat);

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Time:")
			.right(formattedTime)
			.rightColor(timeColor)
			.build());

		panelComponent.setPreferredSize(new Dimension(170, 0));

		return panelComponent.render(graphics);
	}

	private DateTimeFormatter getTimeFormat(LocalTime time)
	{
		return DateTimeFormatter.ofPattern(time.getHour() > 0 ? HOUR_FORMAT : MINUTE_FORMAT);
	}
}
