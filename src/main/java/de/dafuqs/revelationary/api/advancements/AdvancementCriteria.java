package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.advancement_criteria.*;
import net.minecraft.advancements.*;
import net.minecraft.core.registries.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.registries.*;

import java.util.function.*;

public class AdvancementCriteria {
	
	public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister.create(Registries.TRIGGER_TYPE, Revelationary.MOD_ID);
	
	/**
	 * Triggered every time a player gets a new advancement
	 */
	public static Supplier<AdvancementGottenCriterion> ADVANCEMENT_GOTTEN = TRIGGER_TYPES.register("advancement_gotten", AdvancementGottenCriterion::new);
	/**
	 * Triggered every time a player gets a new advancement
	 * matches multiple advancements with optional count parameter
	 */
	public static Supplier<AdvancementCountCriterion> ADVANCEMENT_COUNT = TRIGGER_TYPES.register("advancement_count", AdvancementCountCriterion::new);
	/**
	 * Triggers every time a new block is revealed
	 */
	public static Supplier<HadRevelationCriterion> HAD_REVELATION = TRIGGER_TYPES.register("had_revelation", HadRevelationCriterion::new);
	
	public static void register(IEventBus eventBus) {
		TRIGGER_TYPES.register(eventBus);
	}
}