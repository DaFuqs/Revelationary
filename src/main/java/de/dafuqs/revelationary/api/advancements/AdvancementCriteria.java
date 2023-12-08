package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.advancement_criteria.*;
import net.minecraft.advancement.criterion.Criteria;

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
		ADVANCEMENT_COUNT = Criteria.register(new AdvancementCountCriterion());
		HAD_REVELATION = Criteria.register(new HadRevelationCriterion());
		ADVANCEMENT_GOTTEN = Criteria.register(new AdvancementGottenCriterion());
	}
	
}