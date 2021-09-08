package com.raidspeedruntracker;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import jdk.vm.ci.meta.Local;
import lombok.Data;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.LocalPoint;

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




