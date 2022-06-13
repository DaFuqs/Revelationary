package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.advancement_criteria.HadRevelationCriterion;
import de.dafuqs.revelationary.advancement_criteria.HasAdvancementCriterion;
import net.fabricmc.fabric.mixin.object.builder.CriteriaAccessor;

public class AdvancementCriteria {
	
	public static HasAdvancementCriterion ADVANCEMENT_GOTTEN;
	public static HadRevelationCriterion HAD_REVELATION;
	
	public static void register() {
		ADVANCEMENT_GOTTEN = CriteriaAccessor.callRegister(new HasAdvancementCriterion());
		HAD_REVELATION = CriteriaAccessor.callRegister(new HadRevelationCriterion());
	}
	
}
