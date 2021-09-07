package com.raidspeedruntracker;

import com.google.inject.Provides;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.inject.Inject;

import jdk.jfr.Timespan;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.raids.RaidRoom;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;
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

	private RaidSpeedRunPbs raidSpeedRunPbs;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RaidSpeedRunTrackerOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Setter
	private RaidSpeedRunFileReadWrite fw = new RaidSpeedRunFileReadWrite();

	private LocalPoint doorLocation;

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
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			fw.updateUsername(client.getUsername());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if(event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION || event.getType() == ChatMessageType.GAMEMESSAGE){
			String message = unescapeJavaString(Text.removeTags(event.getMessage()));

			if(message.startsWith("Congratulations - your raid is complete!")){
				Split();
			}
		}
	}

	public String unescapeJavaString(String st) {

		if (st == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder(st.length());

		for (int i = 0; i < st.length(); i++) {
			char ch = st.charAt(i);
			if (ch == '\\') {
				char nextChar = (i == st.length() - 1) ? '\\' : st
					.charAt(i + 1);
				// Octal escape?
				if (nextChar >= '0' && nextChar <= '7') {
					String code = "" + nextChar;
					i++;
					if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
						&& st.charAt(i + 1) <= '7') {
						code += st.charAt(i + 1);
						i++;
						if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
							&& st.charAt(i + 1) <= '7') {
							code += st.charAt(i + 1);
							i++;
						}
					}
					sb.append((char) Integer.parseInt(code, 8));
					continue;
				}
				switch (nextChar) {
					case '\\':
						ch = '\\';
						break;
					case 'b':
						ch = '\b';
						break;
					case 'f':
						ch = '\f';
						break;
					case 'n':
						ch = '\n';
						break;
					case 'r':
						ch = '\r';
						break;
					case 't':
						ch = '\t';
						break;
					case '\"':
						ch = '\"';
						break;
					case '\'':
						ch = '\'';
						break;
					// Hex Unicode: u????
					case 'u':
						if (i >= st.length() - 5) {
							ch = 'u';
							break;
						}
						int code = Integer.parseInt(
							"" + st.charAt(i + 2) + st.charAt(i + 3)
								+ st.charAt(i + 4) + st.charAt(i + 5), 16);
						sb.append(Character.toChars(code));
						i += 5;
						continue;
				}
				i++;
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (e.getId() == 29789)
		{
			clientThread.invokeLater(() -> doorLocation = client.getLocalDestinationLocation());
		}
		else
		{
			clientThread.invokeLater(() -> doorLocation = null);
		}
	}

	@Subscribe
	public void onClientTick(ClientTick e)
	{
		if (doorLocation != null)
		{
			Player local = client.getLocalPlayer();
			if (doorLocation.distanceTo(local.getLocalLocation()) <= Perspective.LOCAL_TILE_SIZE * 2)
			{
				ArrayList<CoxClearCondition> clearConditions = speedRunTracker.getClearConditions();
				boolean cleared = false;

				for (CoxClearCondition clearCondition : clearConditions)
				{
					LocalPoint clearPoint = clearCondition.doorPoint;

					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Clear Point:" + clearPoint.getX() + "," + clearPoint.getY(), null);

					//Check if the current door they went through has been traversed
					if (clearPoint.getX() == doorLocation.getX() &&
						clearPoint.getY() == doorLocation.getY())
					{
						cleared = true;
						break;
					}
				}

				if (!cleared)
				{
					CoxClearCondition newClear = new CoxClearCondition(doorLocation);
					speedRunTracker.clearConditions.add(newClear);
					Split();
				}

				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Door Point:" + doorLocation.getX() + "," + doorLocation.getY(), null);
				doorLocation = null;
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		boolean startedRaid = client.getVar(Varbits.RAID_STATE) == 1;
		boolean inRaid = client.getVar(Varbits.IN_RAID) == 1;
		int teamSize = client.getVar(Varbits.RAID_PARTY_SIZE);

		if (!speedRunTracker.raidInProgress && startedRaid)
		{
			log.debug("Player has started raid");
			speedRunTracker.raidInProgress = startedRaid;
			speedRunTracker.teamSize = teamSize;
			speedRunTracker.startTime = Instant.now();
			speedRunTracker.setCurrentRoom(0);
			BuildSplits();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Player has started raid with size of " + speedRunTracker.teamSize, null);
			overlayManager.add(overlay);
		}
		else if (speedRunTracker.raidInProgress && !startedRaid && !inRaid)
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

	public boolean IsRaidComplete()
	{
		return speedRunTracker.raidComplete;
	}

	public Duration GetPbSplitForCurrentRoom()
	{
		int currentRoomIndex = speedRunTracker.getCurrentRoom();
		RaidRoom currentRoom = speedRunTracker.getRaidRooms()[currentRoomIndex];
		return GetPbSplit(currentRoom);
	}

	public Split GetRaidEndPbSplit(){

		for(int i = 0; i < speedRunTracker.getSplits().length; i++){
			Split split = speedRunTracker.getSplits()[i];
			if(split.raidRoom == RaidRoom.END){
				return split;
			}
		}

		return null;
	}

	public Duration GetPbSplit(RaidRoom raidRoom)
	{
		switch (raidRoom)
		{
			case TEKTON:
				return raidSpeedRunPbs.tektonPbTime;
			case MUTTADILES:
				return raidSpeedRunPbs.mutadilePbTime;
			case GUARDIANS:
				return raidSpeedRunPbs.guardiansPbTime;
			case VESPULA:
				return raidSpeedRunPbs.vespulaPbTime;
			case SHAMANS:
				return raidSpeedRunPbs.shamansPbTime;
			case VASA:
				return raidSpeedRunPbs.vasaPbTime;
			case VANGUARDS:
				return raidSpeedRunPbs.vangaurdsPbTime;
			case MYSTICS:
				return raidSpeedRunPbs.mysticsPbTime;
			case CRABS:
				return raidSpeedRunPbs.crabsPbTime;
			case ICE_DEMON:
				return raidSpeedRunPbs.iceDemonPbTime;
			case TIGHTROPE:
				return raidSpeedRunPbs.tightropePbTime;
			case THIEVING:
				return raidSpeedRunPbs.thievingPbTime;
			case END:
				return raidSpeedRunPbs.raidTotalPbTime;
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
			case END:
				return speedRunTracker.getRaidTotalTime();
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
		Split[] splits = fw.LoadData();
		int numSplits = speedRunTracker.getSplits().length;
		if (splits == null)
		{
			splits = new Split[numSplits];
		}

		for (int i = 0; i < numSplits; i++)
		{
			RaidRoom raidRoom = speedRunTracker.getRaidRooms()[i];
//			Duration pbDuration = GetPbSplit(raidRoom);
////			Duration splitDuration = GetSplit(raidRoom);

			if(splits[i] == null){
				splits[i] = new Split(speedRunTracker.getStartTime(), null, null, raidRoom);
			}
//			Split split = new Split(speedRunTracker.getStartTime(), pbDuration, splitDuration, raidRoom);
			splits[i].startTime = speedRunTracker.getStartTime();
			splits[i].InitSplit();
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
		Split split = speedRunTracker.getSplits()[currentRoom];

		Duration splitDuration = Duration.between(split.startTime, Instant.now());
		Duration s1 = Duration.between(split.startTime, Instant.now());
		String splitTimeString = "";
		if (split.pbDuration != null)
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
			split.s1 = s1;
		}
		else
		{
			split.pbDuration = splitDuration;
			LocalTime splitTime = LocalTime.ofSecondOfDay(splitDuration.getSeconds());
			splitTimeString += splitTime.format(DateTimeFormatter.ofPattern("mm:ss"));
			split.pbString = splitTimeString;
			split.splitString = "-";
		}


		split.splitDuration = splitDuration;


		speedRunTracker.setCurrentRoom(++currentRoom);
		if (currentRoom >= speedRunTracker.getSplits().length)
		{
			speedRunTracker.setRaidComplete(true);
			Duration raidTotalTime = Duration.between(split.startTime, Instant.now());
			speedRunTracker.setRaidTotalTime(raidTotalTime);
			Split pbTime = GetRaidEndPbSplit();
			if (pbTime.s1 == null || raidTotalTime.compareTo(pbTime.pbDuration) < 0)
			{
				Split[] newPbSplits = BuildNewPb(speedRunTracker.Splits);
				fw.SaveData(newPbSplits);
			}
		}
	}

	public Duration GetRaidTotalTime()
	{
		return speedRunTracker.getRaidTotalTime();
	}

	public Split[] BuildNewPb(Split[] splits)
	{
		for(int i = 0; i < splits.length; i++){
			if(splits[i].s1 != null)
			{
				splits[i].pbDuration = splits[i].s1;
			}
		}

		return splits;
	}


}
