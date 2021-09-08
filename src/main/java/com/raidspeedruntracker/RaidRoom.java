package com.raidspeedruntracker;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RaidRoom
{
	TEKTON("Tekton", 0),
	CRABS("Crabs", 1),
	ICE_DEMON("Ice Demon", 2),
	SHAMANS("Shamans", 3),
	VANGUARDS("Vanguards", 4),
	THIEVING("Thieving", 5),
	VESPULA("Vespula", 6),
	TIGHTROPE("Tightrope", 7),
	GUARDIANS("Guardians", 8),
	VASA("Vasa", 9),
	MYSTICS("Mystics", 10),
	MUTTADILES("Muttadiles", 11),
	OLM("Olm", 12);

	private final String name;
	private final int position;

	//Get the next raid room in order
	public RaidRoom getNext(){
		return this.ordinal() < RaidRoom.values().length - 1
			? RaidRoom.values()[this.ordinal() + 1]
			: null;
	}
}

