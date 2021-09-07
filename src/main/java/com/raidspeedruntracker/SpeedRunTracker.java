package com.raidspeedruntracker;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import jdk.vm.ci.meta.Local;
import lombok.Data;

import java.util.UUID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.raids.RaidRoom;

@Data
public class SpeedRunTracker
{
	boolean raidComplete = false;
	boolean raidInProgress = false;

	Duration upperFloorTime;
	Duration middleFloorTime;
	Duration bottomFloorTime;
	Duration olmTime;
	Duration raidTotalTime;

	Duration tektonTime;
	Duration crabsTime;
	Duration iceDemonPopTime;
	Duration iceDemonTime;
	Duration shamansTime;
	Duration vangaurdsTime;
	Duration thievingTime;
	Duration vespulaTime;
	Duration tightropeTime;
	Duration guardiansTime;
	Duration vasaTime;
	Duration mysticsTime;
	Duration mutadileTime;




//	Duration tektonPbTime = Duration.ofSeconds(15);
//	Duration crabsPbTime = Duration.ofSeconds(30);
//	Duration iceDemonPopPbTime;
//	Duration iceDemonPbTime = Duration.ofSeconds(45);
//	Duration shamansPbTime = Duration.ofSeconds(60);
//	Duration vangaurdsPbTime = Duration.ofSeconds(90);
//	Duration thievingPbTime = Duration.ofSeconds(100);
//	Duration vespulaPbTime = Duration.ofSeconds(105);
//	Duration tightropePbTime = Duration.ofSeconds(110);
//	Duration guardiansPbTime = Duration.ofSeconds(115);
//	Duration vasaPbTime = Duration.ofSeconds(120);
//	Duration mysticsPbTime = Duration.ofSeconds(125);
//	Duration mutadilePbTime = Duration.ofSeconds(130);

	int teamSize = -1;

	Instant startTime;

	int currentRoom;
	Split[] Splits = new Split[13];
	RaidRoom[] raidRooms = {
		RaidRoom.TEKTON, RaidRoom.CRABS, RaidRoom.ICE_DEMON, RaidRoom.SHAMANS,
		RaidRoom.VANGUARDS, RaidRoom.THIEVING, RaidRoom.VESPULA, RaidRoom.TIGHTROPE,
		RaidRoom.GUARDIANS, RaidRoom.VASA, RaidRoom.MYSTICS, RaidRoom.MUTTADILES,
		RaidRoom.END
	};

	//	CoxClearCondition[] clearConditions = new CoxClearCondition[13]{
//		new CoxClearCondition(new LocalPoint(6591, 6975), RaidRoom.TEKTON),
//		new CoxClearCondition(new LocalPoint(3135, 8127), RaidRoom.CRABS),
//		new CoxClearCondition(new LocalPoint(7487, 6207), RaidRoom.ICE_DEMON),
//		new CoxClearCondition(new LocalPoint(6719, 6079), RaidRoom.SHAMANS),
//		new CoxClearCondition(new LocalPoint(5567, 8127), RaidRoom.CRABS)
//	};
	ArrayList<CoxClearCondition> clearConditions = new ArrayList<CoxClearCondition>();

	String logId = UUID.randomUUID().toString();

	public SpeedRunTracker()
	{
		clearConditions.add(new CoxClearCondition(new LocalPoint(5567, 8127)));
		clearConditions.add(new CoxClearCondition(new LocalPoint(9791, 6591)));
		clearConditions.add(new CoxClearCondition(new LocalPoint(9663, 4287)));
		clearConditions.add(new CoxClearCondition(new LocalPoint(9663, 3263)));
		clearConditions.add(new CoxClearCondition(new LocalPoint(10815, 6591)));
	}
}

