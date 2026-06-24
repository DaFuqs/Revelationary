package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.advancement_criteria.*;
import net.minecraft.advancements.triggers.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;

public class AdvancementCriteria {
	/**
	 * Triggered every time a player gets a new advancement
	 */
	public static AdvancementGottenCriterion ADVANCEMENT_GOTTEN;
	/**
	 * Triggered every time a player gets a new advancement
	 * matches multiple advancements with optional count parameter
	 */
	public static AdvancementCountCriterion ADVANCEMENT_COUNT;
	/**
	 * Triggers every time a new block is revealed
	 */
	public static HadRevelationCriterion HAD_REVELATION;
	
	public static void register() {
		ADVANCEMENT_COUNT = register("advancement_count", new AdvancementCountCriterion());
		HAD_REVELATION = register("had_revelation", new HadRevelationCriterion());
		ADVANCEMENT_GOTTEN = register("advancement_gotten", new AdvancementGottenCriterion());
	}

	private static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
		return (T) (Registry.register(BuiltInRegistries.TRIGGER_TYPES, Identifier.fromNamespaceAndPath(Revelationary.MOD_ID, name), criterion));
	}

}