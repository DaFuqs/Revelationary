package de.dafuqs.revelationary.config;

import com.google.gson.*;
import de.dafuqs.revelationary.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class RevelationaryConfig {

	private static final File CONFIG_FILE_PATH = new File(FabricLoader.getInstance().getConfigDir().toFile(), "Revelationary.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static Config CONFIG = null;

	public static Config get() {
		if(CONFIG == null) {
			load();
		}
		return CONFIG;
	}

	private static void load() {
		if (!CONFIG_FILE_PATH.exists()) {
			try {
				CONFIG_FILE_PATH.createNewFile();
				save();
			} catch (IOException e) {
				Revelationary.logError("Could not generate config file under " + CONFIG_FILE_PATH.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
			}
		} else {
			try {
				CONFIG = GSON.fromJson(new FileReader(CONFIG_FILE_PATH), Config.class);
			} catch (FileNotFoundException e) {
				Revelationary.logError("Could not load config file under " + CONFIG_FILE_PATH.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
			}
		}
	}

	private static void save() {
		try {
			FileWriter writer = new FileWriter(CONFIG_FILE_PATH);
			GSON.toJson(CONFIG, writer);
			writer.close();
		} catch (IOException e) {
			Revelationary.logError("Could not save config file under " + CONFIG_FILE_PATH.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
		}
	}

	public static class Config {
		public boolean PreventMiningOfUnrevealedBlocks = false;
		public boolean UseTargetBlockOrItemNameInsteadOfScatter = false;
		public String NameForUnrevealedBlocks = "";
		public String NameForUnrevealedItems = "";

		public Config() {

		}

	}

}