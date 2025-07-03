package de.dafuqs.revelationary;

import com.google.gson.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.*;

import java.util.*;

public class RevelationDataLoader extends SimpleJsonResourceReloadListener {
	public static final RevelationDataLoader INSTANCE = new RevelationDataLoader();
	
	private RevelationDataLoader() {
		super(new Gson(), "revelations");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
		prepared.forEach((identifier, jsonElement) -> RevelationRegistry.registerFromJson(jsonElement.getAsJsonObject()));
		RevelationRegistry.deepTrim();
	}
	
}