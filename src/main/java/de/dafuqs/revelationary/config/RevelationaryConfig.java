package de.dafuqs.revelationary.config;

import com.google.gson.*;
import de.dafuqs.revelationary.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class RevelationaryConfig {
	public static Config CONFIG = new Config();

	static File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "Revelationary.json");
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static void load() {
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
				save();
			} catch (IOException e) {
				Revelationary.logError("Could not generate config file under " + configFile.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
			}
		} else {
			try {
				CONFIG = gson.fromJson(new FileReader(configFile), Config.class);
			} catch (FileNotFoundException e) {
				Revelationary.logError("Could not load config file under " + configFile.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
			}
		}
	}
	public static void save() {
		try {
			FileWriter writer = new FileWriter(configFile);
			gson.toJson(CONFIG, writer);
			writer.close();
		} catch (IOException e) {
			Revelationary.logError("Could not save config file under " + configFile.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
		}
	}

	public static class Config {
		public boolean PreventMiningOfUnrevealedBlocks = false;
		public boolean UseTargetBlockOrItemNameInsteadOfScatter = false;
		public String NameForUnrevealedBlocks = "";
		public String NameForUnrevealedItems = "";

		public Config() {}
	}
}
