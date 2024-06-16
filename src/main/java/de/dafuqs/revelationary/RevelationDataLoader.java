package de.dafuqs.revelationary;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class RevelationDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
	public static final RevelationDataLoader INSTANCE = new RevelationDataLoader();
	
	private RevelationDataLoader() {
		super(new Gson(), "revelations");
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		prepared.forEach((identifier, jsonElement) -> RevelationRegistry.registerFromJson(jsonElement.getAsJsonObject()));
		RevelationRegistry.deepTrim();
	}
	
	@Override
	public Identifier getFabricId() {
		return Identifier.of(Revelationary.MOD_ID, "revelations");
	}
}