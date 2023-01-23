package de.dafuqs.revelationary.config;

import com.google.gson.*;
import de.dafuqs.revelationary.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.*;

import java.io.*;

public class RevelarionaryConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public boolean UnrevealedBlocksAreUnbreakable = false;
	public boolean UseTargetBlockOrItemNameInsteadOfScatter = false;
	public String NameForUnrevealedBlocks = "";
	public String NameForUnrevealedItems = "";

	public RevelarionaryConfig() {
		File configFile = FabricLoader.getInstance().getConfigDir().resolve("Revelationary.json").toFile();
		FileReader configReader;
		try {
			configReader = new FileReader(configFile);
		} catch (Exception e) {
			try {
				configFile.createNewFile();
				FileWriter myWriter = new FileWriter(configFile);
				myWriter.write(DEFAULT_CONFIG);
				myWriter.close();

				configReader = new FileReader(configFile);
			} catch (IOException ioException) {
				Revelationary.logError("Could not generate config file under " + configFile.getAbsolutePath() + ".\n" + ioException.getLocalizedMessage());
				return;
			}
		}

		JsonObject jsonObject;
		try {
			jsonObject = JsonHelper.deserialize(GSON, configReader, JsonObject.class);
		} catch (Exception e) {
			Revelationary.logError("Could not parse Revelationary.json: " + e.getLocalizedMessage());
			return;
		}

		UnrevealedBlocksAreUnbreakable = JsonHelper.getBoolean(jsonObject, "UnrevealedBlocksAreUnbreakable", false);
		UseTargetBlockOrItemNameInsteadOfScatter = JsonHelper.getBoolean(jsonObject, "UseTargetBlockOrItemNameInsteadOfScatter", false);
		NameForUnrevealedBlocks = JsonHelper.getString(jsonObject, "NameForUnrevealedBlocks", "");
		NameForUnrevealedItems = JsonHelper.getString(jsonObject, "NameForUnrevealedItems", "");
	}

	private static final String DEFAULT_CONFIG = """
{
	"UnrevealedBlocksAreUnbreakable": false,
	"UseTargetBlockOrItemNameInsteadOfScatter": false,
	"NameForUnrevealedBlocks": "",
	"NameForUnrevealedItems": ""
}
""";
	
}
