package de.dafuqs.revelationary.advancement_criteria;

import com.google.gson.JsonObject;
import de.dafuqs.revelationary.Revelationary;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class AdvancementGottenCriterion extends AbstractCriterion<AdvancementGottenCriterion.Conditions> {
	
	public static final Identifier ID = new Identifier(Revelationary.MOD_ID, "advancement_gotten");
	
	public static AdvancementGottenCriterion.Conditions create(Identifier id) {
		return new AdvancementGottenCriterion.Conditions(EntityPredicate.Extended.EMPTY, id);
	}
	
	public Identifier getId() {
		return ID;
	}
	
	public AdvancementGottenCriterion.Conditions conditionsFromAdvancementIdentifier(EntityPredicate.Extended extended, Identifier identifier) {
		return new AdvancementGottenCriterion.Conditions(extended, identifier);
	}
	
	public AdvancementGottenCriterion.Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
		Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "advancement_identifier"));
		return new AdvancementGottenCriterion.Conditions(extended, identifier);
	}
	
	public void trigger(ServerPlayerEntity player, Advancement advancement) {
		this.trigger(player, (conditions) -> conditions.matches(advancement));
	}
	
	public static class Conditions extends AbstractCriterionConditions {
		private final Identifier advancementIdentifier;
		
		public Conditions(EntityPredicate.Extended player, Identifier advancementIdentifier) {
			super(ID, player);
			this.advancementIdentifier = advancementIdentifier;
		}
		
		public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
			JsonObject jsonObject = super.toJson(predicateSerializer);
			jsonObject.addProperty("advancement_identifier", this.advancementIdentifier.toString());
			return jsonObject;
		}
		
		public boolean matches(Advancement advancement) {
			return this.advancementIdentifier.equals(advancement.getId());
		}
		
		public Identifier getAdvancementIdentifier() {
			return advancementIdentifier;
		}
	}
	
}
