package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.advancement_criteria.AdvancementGottenCriterion;
import de.dafuqs.revelationary.advancement_criteria.HadRevelationCriterion;
import net.fabricmc.fabric.mixin.object.builder.CriteriaAccessor;

public class AdvancementCriteria {
	
	/**
	 * Triggered every time a player gets a new advancement
	 */
	public static AdvancementGottenCriterion ADVANCEMENT_GOTTEN;
	/**
	 * Triggers every time a new block is revealed
	 */
	public static HadRevelationCriterion HAD_REVELATION;
	
	public static void register() {
		ADVANCEMENT_GOTTEN = CriteriaAccessor.callRegister(new AdvancementGottenCriterion());
		HAD_REVELATION = CriteriaAccessor.callRegister(new HadRevelationCriterion());
	}
	
}