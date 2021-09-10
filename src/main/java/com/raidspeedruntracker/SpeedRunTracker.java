package com.raidspeedruntracker;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SpeedRunTracker
{
	transient boolean raidComplete = false;
	transient boolean raidInProgress = false;
	transient boolean raidStarted = false;

	transient Instant startTime = Instant.now();

	transient RaidRoom currentRoom = RaidRoom.TEKTON;

	int teamSize = -1;

	List<SpeedRun> speedRuns = new ArrayList<>();
	SpeedRun currentSpeedRun = new SpeedRun();
}




