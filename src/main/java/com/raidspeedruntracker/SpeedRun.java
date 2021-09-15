package com.raidspeedruntracker;

import lombok.Data;

@Data
public class SpeedRun
{
	int teamSize = -1;
	Split[] splits = new Split[13];

	public SpeedRun()
	{
		RaidRoom r = RaidRoom.TEKTON;
		for (int i = 0; i < splits.length; i++)
		{
			splits[i] = new Split(r);
			r = r.getNext();
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null){
			return false;
		}

		SpeedRun s = (SpeedRun) obj;

		return this.teamSize == s.teamSize;
	}
}
