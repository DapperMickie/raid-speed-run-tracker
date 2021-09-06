package com.raidspeedruntracker;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import lombok.Data;

import java.util.UUID;
import net.runelite.client.plugins.raids.RaidRoom;

@Data
public class SpeedRunTracker {
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

	Duration upperFloorPbTime;
	Duration middleFloorPbTime;
	Duration bottomFloorPbTime;
	Duration olmPbTime;
	Duration raidTotalPbTime;

//	Duration tektonPbTime;
//	Duration crabsPbTime;
//	Duration iceDemonPopPbTime;
//	Duration iceDemonPbTime;
//	Duration shamansPbTime;
//	Duration vangaurdsPbTime;
	Duration tektonPbTime = Duration.ofSeconds(15);
	Duration crabsPbTime = Duration.ofSeconds(30);
	Duration iceDemonPopPbTime;
	Duration iceDemonPbTime = Duration.ofSeconds(45);
	Duration shamansPbTime = Duration.ofSeconds(60);
	Duration vangaurdsPbTime = Duration.ofSeconds(90);
	Duration thievingPbTime;
	Duration vespulaPbTime;
	Duration tightropePbTime;
	Duration guardiansPbTime;
	Duration vasaPbTime;
	Duration mysticsPbTime;
	Duration mutadilePbTime;

    int teamSize = -1;

	Instant startTime;

	int currentRoom;
	Split[] Splits = new Split[12];
	RaidRoom[] raidRooms = {
		RaidRoom.TEKTON, RaidRoom.CRABS, RaidRoom.ICE_DEMON,
		RaidRoom.VANGUARDS, RaidRoom.THIEVING, RaidRoom.VESPULA, RaidRoom.TIGHTROPE,
		RaidRoom.GUARDIANS, RaidRoom.VASA, RaidRoom.MYSTICS, RaidRoom.MUTTADILES,
		RaidRoom.END
	};

    String logId = UUID.randomUUID().toString();
}

