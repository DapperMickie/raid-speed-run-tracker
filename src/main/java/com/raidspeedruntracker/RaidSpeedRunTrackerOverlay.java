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

		Duration elapsed = Duration.between(startTime, Instant.now());
		time = LocalTime.ofSecondOfDay(elapsed.getSeconds());

		String formattedTime = time.format(DateTimeFormatter.ofPattern("mm:ss"));

		Color timeColor = Color.WHITE;

		Split[] splits = raidSpeedRunTrackerPlugin.GetSplits();
		int offset = config.numLines() / 2;
		if (currentRoomIndex - offset < 0)
		{
			if (Math.abs(currentRoomIndex - offset) == offset)
			{
				offset = 0;
			}
			else
			{
				offset = Math.abs(currentRoomIndex - offset);
			}
		}
		int startRoomIndex = currentRoomIndex - offset;

		Split[] splitsToShow;



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
						timeColor = Color.GREEN;
						splitString += "-";
					}
					else
					{
						splitColor = Color.RED;
						timeColor = Color.RED;
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

			panelComponent.getChildren().add(LineComponent.builder()
				.left(split.raidRoom.getName() + (currentRow ? " <-" : ""))
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
