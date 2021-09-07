package com.raidspeedruntracker;

import com.google.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.runelite.api.Client;
import net.runelite.client.plugins.raids.RaidRoom;
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


		String overlayTitle = "CoX Splits:";

		panelComponent.getChildren().add(TitleComponent.builder()
			.text(overlayTitle)
			.color(Color.white)
			.build());

		Instant startTime = raidSpeedRunTrackerPlugin.GetStartTime();
		int currentRoomIndex = raidSpeedRunTrackerPlugin.GetCurrentRoomIndex();

		LocalTime time;
		Duration elapsed;

		Color timeColor = Color.WHITE;

		if (raidSpeedRunTrackerPlugin.IsRaidComplete())
		{
			elapsed = raidSpeedRunTrackerPlugin.GetRaidTotalTime();
			Split raidPbTime = raidSpeedRunTrackerPlugin.GetRaidEndPbSplit();
			if (raidPbTime != null && raidPbTime.originalPbDuration != null )
			{
				if (elapsed.compareTo(raidPbTime.originalPbDuration) < 0)
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

		String formattedTime = time.format(DateTimeFormatter.ofPattern("mm:ss"));

		Split[] splits = raidSpeedRunTrackerPlugin.GetSplits();

		int offset = config.numLines() / 2;
		int startRoomIndex = currentRoomIndex;

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

		for (int i = startRoomIndex; i < config.numLines() + startRoomIndex; i++)
		{
			Split split = splits[i];

			String splitString = "";
			Color splitColor = Color.WHITE;
			boolean currentRow = i == currentRoomIndex;

			if (currentRow)
			{
				if (split.splitDuration == null && split.pbDuration != null)
				{
					Duration splitDuration = Duration.between(split.startTime, Instant.now());
					splitDuration = splitDuration.minus(split.pbDuration);

					LocalTime splitTime = LocalTime.ofSecondOfDay(splitDuration.abs().getSeconds());

					if (splitDuration.isNegative())
					{
						splitColor = Color.GREEN;
						if(!raidSpeedRunTrackerPlugin.IsRaidComplete())
						{
							timeColor = Color.GREEN;
						}
						splitString += "-";
					}
					else
					{
						splitColor = Color.RED;
						if(!raidSpeedRunTrackerPlugin.IsRaidComplete())
						{
							timeColor = Color.RED;
						}
						splitString += "+";
					}

					splitString += splitTime.format(DateTimeFormatter.ofPattern("mm:ss"));
				}
			}
			else
			{
				if (split.splitString != null)
				{
					splitString = split.splitString;
					splitColor = split.splitColor;
				}
			}

			String name = split.raidRoom.getName().toLowerCase().equals("end") ? "Olm" : split.raidRoom.getName();

			panelComponent.getChildren().add(LineComponent.builder()
				.left(name + (currentRow ? " <-" : ""))
				.right(splitString + " " + split.pbString)
				.rightColor(splitColor)
				.build());
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Time:")
			.right(formattedTime)
			.rightColor(timeColor)
			.build());

		panelComponent.setPreferredSize(new Dimension(170, 0));

		return panelComponent.render(graphics);
	}
}
