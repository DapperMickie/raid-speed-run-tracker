package com.raidspeedruntracker;

import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.raids.RaidRoom;

public class CoxClearCondition
{
	boolean cleared = false;
	RaidRoom raidRoom;
	LocalPoint doorPoint;

	public CoxClearCondition(LocalPoint doorPoint)
	{
		this.doorPoint = doorPoint;
		this.raidRoom = raidRoom;
	}

	@Override
	public boolean equals(Object obj)
	{
		CoxClearCondition c = (CoxClearCondition) obj;
		if (this.doorPoint.getX() == c.doorPoint.getX() &&
			this.doorPoint.getY() == c.doorPoint.getY())
		{
			return true;
		}
		return false;
	}
}
