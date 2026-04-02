package de.dafuqs.revelationary.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;

@Mixin(ClientAdvancements.class)
public interface AccessorClientAdvancementManager {
	@Accessor
	Map<AdvancementHolder, AdvancementProgress> getProgress();
}