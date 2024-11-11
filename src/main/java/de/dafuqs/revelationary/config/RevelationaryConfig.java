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
			CONFIG = Config.load();
		}
		return CONFIG;
	}

	public static class Config {
		public boolean PreventMiningOfUnrevealedBlocks = false;
		public boolean UseTargetBlockOrItemNameInsteadOfScatter = false;
		public boolean HideCloakedEntriesFromRecipeViewers = false;
		public String NameForUnrevealedBlocks = "";
		public String NameForUnrevealedItems = "";

		public Config() { }

		private static Config load() {
			if (!CONFIG_FILE_PATH.exists()) {
				try {
					CONFIG_FILE_PATH.createNewFile();
					Config config = new Config();
					config.save();
					return config;
				} catch (IOException e) {
					Revelationary.logError("Could not generate config file under " + CONFIG_FILE_PATH.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
				}
			}

			try {
				return GSON.fromJson(new FileReader(CONFIG_FILE_PATH), Config.class);
			} catch (FileNotFoundException e) {
				Revelationary.logError("Could not load config file under " + CONFIG_FILE_PATH.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
			}

			return new Config();
		}

		private void save() {
			try {
				FileWriter writer = new FileWriter(CONFIG_FILE_PATH);
				GSON.toJson(this, writer);
				writer.close();
			} catch (IOException e) {
				Revelationary.logError("Could not save config file under " + CONFIG_FILE_PATH.getAbsolutePath() + ".\n" + e.getLocalizedMessage());
			}
		}

	}

}
