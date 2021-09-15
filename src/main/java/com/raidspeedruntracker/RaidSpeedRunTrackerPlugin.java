package com.raidspeedruntracker;

import com.google.inject.Provides;
import static com.raidspeedruntracker.CoxUtil.getroom_type;
import java.awt.Color;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.raids.Raid;
import net.runelite.client.plugins.raids.solver.LayoutSolver;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

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

	@Setter
	private RaidSpeedRunFileReadWrite fw = new RaidSpeedRunFileReadWrite();

	private int cryp[] = new int[16], cryx[] = new int[16], cryy[] = new int[16];

	private static final String RAID_COMPLETE_MESSAGE = "Congratulations - your raid is complete!";
	private static final String RAID_START_MESSAGE = "The raid has begun!";
	private static final int CM_FLAG_VARBIT = 6385;

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			fw.updateUsername(client.getUsername());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION
			|| event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			String message = Text.removeTags(event.getMessage());

			if (message.startsWith(RAID_COMPLETE_MESSAGE))
			{
				split();
			}

			if (message.startsWith(RAID_START_MESSAGE))
			{
				speedRunTracker.setRaidStarted(true);
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		boolean raidStarted = client.getVar(Varbits.RAID_STATE) > 0;
		boolean inRaid = client.getVar(Varbits.IN_RAID) == 1;
		boolean isCm = client.getVarbitValue(CM_FLAG_VARBIT) == 1;
		int teamSize = client.getVar(Varbits.RAID_PARTY_SIZE);

		if (speedRunTracker.isRaidStarted()
			&& inRaid
			&& !speedRunTracker.isRaidInProgress()
			&& isCm
		)
		{
			speedRunTracker.setTeamSize(teamSize);
			speedRunTracker.setRaidInProgress(true);
			speedRunTracker.setStartTime(Instant.now());

			loadSplits(teamSize);
			overlayManager.add(overlay);

		}
		else if (speedRunTracker.raidInProgress && !inRaid)
		{
			ResetSpeedRunTracker();
		}
		//Sometimes when the raid starts, the raid size isn't correct. If the team gets larger, set the new size and re-load the splits
		else if (speedRunTracker.isRaidInProgress() && inRaid && teamSize > speedRunTracker.getTeamSize())
		{
			speedRunTracker.setTeamSize(teamSize);
			loadSplits(teamSize);
		}
	}

	//Credit to dey0 for this: https://github.com/dey0/pluginhub-plugins
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject go = e.getGameObject();
		switch (go.getId())
		{
			case 26209: // shamans/thieving/guardians
			case 29741: // mystics
			case 29749: // tightrope
			case 29753: // crabs
			case 29754:
			case 29755:
			case 29756:
			case 29757:
			case 29876: // ice
			case 30016: // vasa
			case 30017: // tekton/vanguards
			case 30018: // mutt
			case 30070: // vespula
				Point pt = go.getSceneMinLocation();
				int p = go.getPlane();
				int x = pt.getX();
				int y = pt.getY();
				int template = client.getInstanceTemplateChunks()[p][x / 8][y / 8];
				int roomtype = getroom_type(template);
				if (roomtype < 16)
				{
					// add obstacle to list
					cryp[roomtype] = p;
					cryx[roomtype] = x + client.getBaseX();
					cryy[roomtype] = y + client.getBaseY();
				}
				break;
		}
	}

	//Credit to dey0 for this: https://github.com/dey0/pluginhub-plugins
	@Subscribe
	public void onClientTick(ClientTick e)
	{
		for (int i = 0; i < 16; i++)
		{
			if (this.cryp[i] == -1)
			{
				continue;
			}
			int p = cryp[i];
			int x = cryx[i] - client.getBaseX();
			int y = cryy[i] - client.getBaseY();
			if (p != client.getPlane()
				|| x < 0 || x >= 104
				|| y < 0 || y >= 104)
			{
				this.cryp[i] = -1;
				continue;
			}
			int flags = client.getCollisionMaps()[p].getFlags()[x][y];
			if ((flags & 0x100) == 0)
			{
				split();
				this.cryp[i] = -1;
			}
		}
	}

	@Provides
	RaidSpeedRunTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidSpeedRunTrackerConfig.class);
	}

	public Split getSplit(RaidRoom raidRoom)
	{
		SpeedRun speedRun = speedRunTracker.getCurrentSpeedRun();
		for (Split split : speedRun.getSplits())
		{
			if (split.raidRoom == raidRoom)
			{
				return split;
			}

		}
		return null;
	}

	public Split[] getSplits()
	{
		return speedRunTracker.getCurrentSpeedRun().getSplits();
	}


	public RaidRoom getCurrentRoom()
	{
		return speedRunTracker.getCurrentRoom();
	}

	public Instant GetStartTime()
	{
		return speedRunTracker.getStartTime();
	}

	public boolean IsRaidComplete()
	{
		return speedRunTracker.raidComplete;
	}

	private void loadSplits(int teamSize)
	{
		//When the raid starts, begin to load the split data
		List<SpeedRun> speedRuns = fw.LoadData();

		if (speedRuns != null)
		{
			for (SpeedRun speedRun : speedRuns)
			{
				if (speedRun.getTeamSize() == teamSize)
				{
					for (Split split : speedRun.getSplits())
					{
						split.setOriginalPbDuration(split.getOriginalPbDuration());
					}
					speedRunTracker.setCurrentSpeedRun(speedRun);
				}
			}

			speedRunTracker.setSpeedRuns(speedRuns);
		}

		speedRunTracker.getCurrentSpeedRun().setTeamSize(teamSize);
	}

	private void split()
	{
		RaidRoom currentRoom = speedRunTracker.getCurrentRoom();
		Split split = getSplit(currentRoom);
		Instant startTime = speedRunTracker.getStartTime();

		Duration timeDifference = Duration.between(startTime, Instant.now());
		if (split.pbDuration != null)
		{
			//In the event that this run is a pb, set the pb before calculating time difference
			split.setNewPbDuration(timeDifference);

			//Calculate the time difference between the current split vs pb
			timeDifference = timeDifference.minus(split.pbDuration);
			split.setTimeDifference(timeDifference);
			if (timeDifference.isNegative())
			{
				split.setSplitColor(Color.GREEN);
			}
			else
			{
				split.setSplitColor(Color.RED);
			}
		}
		else
		{
			//If there have been no pbs, set the pb to be the current split and time difference to be -
			split.setPbDuration(timeDifference);
			split.setNewPbDuration(timeDifference);
			split.setTimeDifference(timeDifference);
			split.setSplitString("-");
		}

		//If the current room is olm, end the raid and check if a new pb has been accomplished
		if (currentRoom == RaidRoom.OLM)
		{
			Duration raidTotalTime = Duration.between(startTime, Instant.now());
			Split pbTime = getSplit(RaidRoom.OLM);
			//If original pb time doesn't exist or if the new time beats the old pb, save the splits
			if (pbTime.originalPbDuration == null
				|| raidTotalTime.compareTo(pbTime.originalPbDuration) < 0)
			{
				SpeedRun currentRun = speedRunTracker.getCurrentSpeedRun();
				//Set the current speedrun's splits to be the new pb
				Split[] newPbSplits = buildNewPb(currentRun.getSplits());
				//Replace the old speedrun with matching team size to the one with the new pb
				List<SpeedRun> speedRuns = speedRunTracker.getSpeedRuns();

				boolean added = false;

				for (SpeedRun speedRun : speedRuns)
				{
					if (speedRunTracker.getCurrentSpeedRun() == speedRun)
					{
						speedRun.setSplits(newPbSplits);
						added = true;
					}
				}

				if (!added)
				{
					speedRuns = new ArrayList<>(speedRuns);
					speedRuns.add(currentRun);
					speedRunTracker.setSpeedRuns(speedRuns);
				}

				fw.SaveData(speedRuns);
			}

			speedRunTracker.setRaidComplete(true);
		}
		else
		{
			speedRunTracker.setCurrentRoom(currentRoom.getNext());
		}
	}

	private Split[] buildNewPb(Split[] splits)
	{
		for (int i = 0; i < splits.length; i++)
		{
			if (splits[i].newPbDuration != null)
			{
				splits[i].pbDuration = splits[i].newPbDuration;
			}
		}

		return splits;
	}

	private void ResetSpeedRunTracker()
	{
		speedRunTracker = new SpeedRunTracker();
		overlayManager.remove(overlay);
	}
}
