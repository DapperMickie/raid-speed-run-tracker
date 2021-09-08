package com.raidspeedruntracker;

import java.time.Duration;
import java.time.Instant;
import lombok.Data;

@Data
public class SpeedRunTracker
{
	transient boolean raidComplete = false;
	transient boolean raidInProgress = false;

	transient Instant startTime = Instant.now();

	transient RaidRoom currentRoom = RaidRoom.TEKTON;

	Split[] splits = new Split[13];
	int teamSize = -1;

	public SpeedRunTracker()
	{
		RaidRoom r = RaidRoom.TEKTON;
		for(int i = 0; i < splits.length; i++){
			splits[i] = new Split(r);
			r = r.getNext();
		}
	}
}




