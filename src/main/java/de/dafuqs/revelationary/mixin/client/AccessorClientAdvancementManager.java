package de.dafuqs.revelationary.mixin.client;

import net.minecraft.advancements.*;
import net.minecraft.client.multiplayer.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.*;

@Mixin(ClientAdvancements.class)
public interface AccessorClientAdvancementManager {
	@Accessor
	Map<AdvancementHolder, AdvancementProgress> getProgress();
	
}