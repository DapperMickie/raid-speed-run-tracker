package com.raidspeedruntracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import lombok.Getter;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class RaidSpeedRunFileReadWrite
{
	@Getter
	private String username;
	private String dir;


	public void SaveData(Split[] splits)
	{
		try
		{
			Gson gson = new GsonBuilder().create();
			String fileName = dir;

			FileWriter fw = new FileWriter(fileName, false);

			gson.toJson(splits, fw);

			fw.append("\n");

			fw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Split[] LoadData()
	{
		try
		{
			Gson gson = new GsonBuilder().create();
			String fileName = dir;

			FileReader fr = new FileReader(fileName);
			return gson.fromJson(fr, Split[].class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public void createFolder()
	{
		File dir = new File(RUNELITE_DIR, "speed-run tracker");
		dir.mkdir();
		dir = new File(dir, username);
		dir.mkdir();
		File newSpeedRunTrackerFile = new File(dir + "\\speed_run_tracker.log");

		try
		{
			newSpeedRunTrackerFile.createNewFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		this.dir = newSpeedRunTrackerFile.getAbsolutePath();
	}

	public void updateUsername(final String username)
	{
		this.username = username;
		createFolder();
	}
}
