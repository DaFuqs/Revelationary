package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.advancements.*;
import net.minecraft.advancements.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementTrackerMixin {
	@Shadow
	private ServerPlayer player;
	
	@Inject(at = @At("RETURN"), method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z")
	public void revelationary$triggerAdvancementCriteria(AdvancementHolder advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
		AdvancementProgress advancementProgress = ((PlayerAdvancements) (Object) this).getOrStartProgress(advancement);
		if (advancementProgress.isDone()) {
			AdvancementCriteria.ADVANCEMENT_GOTTEN.trigger(player, advancement);
			AdvancementCriteria.ADVANCEMENT_COUNT.trigger(player);
			
			List<Block> revelations = RevelationRegistry.getBlockEntries(advancement.id());
			for (Block revelation : revelations) {
				AdvancementCriteria.HAD_REVELATION.trigger(player, revelation);
			}
		}
	}
}
