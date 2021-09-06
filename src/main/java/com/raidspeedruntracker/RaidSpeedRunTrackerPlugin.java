package com.raidspeedruntracker;

import com.google.inject.Provides;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;

import jdk.jfr.Timespan;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.raids.RaidRoom;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(
	name = "Raid Speed Run Tracker"
)
public class RaidSpeedRunTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RaidSpeedRunTrackerConfig config;

	@Inject
	private SpeedRunTracker speedRunTracker;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RaidSpeedRunTrackerOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
//		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
//		{
//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
//		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getMessage().toLowerCase().contains("split"))
		{
			Split();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		boolean inRaid = client.getVar(Varbits.RAID_STATE) == 1;
		int teamSize = client.getVar(Varbits.RAID_PARTY_SIZE);

		if (!speedRunTracker.raidInProgress && inRaid)
		{
			log.debug("Player has started raid");
			speedRunTracker.raidInProgress = inRaid;
			speedRunTracker.teamSize = teamSize;
			speedRunTracker.startTime = Instant.now();
			speedRunTracker.setCurrentRoom(0);
			BuildSplits();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Player has started raid with size of " + speedRunTracker.teamSize, null);
			overlayManager.add(overlay);

		}
		else if (speedRunTracker.raidInProgress && !inRaid)
		{
			log.debug("Player has exited raid");

			Duration elapsed = Duration.between(speedRunTracker.getStartTime(), Instant.now());
			LocalTime time = LocalTime.ofSecondOfDay(elapsed.getSeconds());

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Player has exited raid after " + time.format(DateTimeFormatter.ofPattern("mm:ss")), null);
			ResetSpeedRunTracker();
		}
	}

	@Provides
	RaidSpeedRunTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidSpeedRunTrackerConfig.class);
	}

	private void ResetSpeedRunTracker()
	{
		speedRunTracker = new SpeedRunTracker();
		overlayManager.remove(overlay);
	}

	public String GetCurrentTeamSize()
	{
		return String.valueOf(speedRunTracker.getTeamSize());
	}

	public Instant GetStartTime()
	{
		return speedRunTracker.getStartTime();
	}

	public boolean IsInRaid()
	{
		return speedRunTracker.isRaidInProgress();
	}

	public Duration GetPbSplitForCurrentRoom()
	{
		int currentRoomIndex = speedRunTracker.getCurrentRoom();
		RaidRoom currentRoom = speedRunTracker.getRaidRooms()[currentRoomIndex];
		return GetPbSplit(currentRoom);
	}

	public Duration GetPbSplit(RaidRoom raidRoom)
	{
		switch (raidRoom)
		{
			case TEKTON:
				return speedRunTracker.getTektonPbTime();
			case MUTTADILES:
				return speedRunTracker.getMutadilePbTime();
			case GUARDIANS:
				return speedRunTracker.getGuardiansPbTime();
			case VESPULA:
				return speedRunTracker.getVespulaPbTime();
			case SHAMANS:
				return speedRunTracker.getShamansPbTime();
			case VASA:
				return speedRunTracker.getVasaPbTime();
			case VANGUARDS:
				return speedRunTracker.getVangaurdsPbTime();
			case MYSTICS:
				return speedRunTracker.getMysticsPbTime();
			case CRABS:
				return speedRunTracker.getCrabsPbTime();
			case ICE_DEMON:
				return speedRunTracker.getIceDemonPbTime();
			case TIGHTROPE:
				return speedRunTracker.getTightropePbTime();
			case THIEVING:
				return speedRunTracker.getThievingPbTime();
			default:
				return null;
		}
	}

	public Duration GetSplit(RaidRoom raidRoom)
	{
		switch (raidRoom)
		{
			case TEKTON:
				return speedRunTracker.getTektonTime();
			case MUTTADILES:
				return speedRunTracker.getMutadileTime();
			case GUARDIANS:
				return speedRunTracker.getGuardiansTime();
			case VESPULA:
				return speedRunTracker.getVespulaTime();
			case SHAMANS:
				return speedRunTracker.getShamansTime();
			case VASA:
				return speedRunTracker.getVasaTime();
			case VANGUARDS:
				return speedRunTracker.getVangaurdsTime();
			case MYSTICS:
				return speedRunTracker.getMysticsTime();
			case CRABS:
				return speedRunTracker.getCrabsTime();
			case ICE_DEMON:
				return speedRunTracker.getIceDemonTime();
			case TIGHTROPE:
				return speedRunTracker.getTightropeTime();
			case THIEVING:
				return speedRunTracker.getThievingTime();
			default:
				return null;
		}
	}

	public RaidRoom GetRaidRoom(int index)
	{
		return speedRunTracker.getRaidRooms()[index];
	}

	public int GetCurrentRoomIndex()
	{
		return speedRunTracker.getCurrentRoom();
	}

	public void BuildSplits()
	{
		int numSplits = speedRunTracker.getSplits().length;
		Split[] splits = new Split[numSplits];

		for (int i = 0; i < numSplits; i++)
		{
			RaidRoom raidRoom = speedRunTracker.getRaidRooms()[i];
			Duration pbDuration = GetPbSplit(raidRoom);
			Duration splitDuration = GetSplit(raidRoom);

			Split split = new Split(speedRunTracker.getStartTime(), pbDuration, splitDuration, raidRoom);
			splits[i] = split;
		}

		speedRunTracker.setSplits(splits);
	}

	public Split[] GetSplits()
	{
		return speedRunTracker.getSplits();
	}

	public void Split()
	{
		int currentRoom = speedRunTracker.getCurrentRoom();
		;
		Split split = speedRunTracker.getSplits()[currentRoom];

		Duration splitDuration = Duration.between(split.startTime, Instant.now());
		String splitTimeString = "";
		if(split.pbDuration != null)
		{
			splitDuration = splitDuration.minus(split.pbDuration);
			if (splitDuration.isNegative())
			{
				split.splitColor = Color.GREEN;
				splitTimeString += "-";
			}
			else
			{
				split.splitColor = Color.RED;
				splitTimeString += "+";
			}

			LocalTime splitTime = LocalTime.ofSecondOfDay(splitDuration.abs().getSeconds());
			splitTimeString += splitTime.format(DateTimeFormatter.ofPattern("mm:ss"));
			split.splitString = splitTimeString;
		}
		else{
			split.pbDuration = splitDuration;
			LocalTime splitTime = LocalTime.ofSecondOfDay(splitDuration.getSeconds());
			splitTimeString += splitTime.format(DateTimeFormatter.ofPattern("mm:ss"));
			split.pbString = splitTimeString;
			split.splitString = "-";
		}


		split.splitDuration = splitDuration;


		speedRunTracker.setCurrentRoom(++currentRoom);
	}


}
