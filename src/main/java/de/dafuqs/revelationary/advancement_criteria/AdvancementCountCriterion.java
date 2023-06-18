package de.dafuqs.revelationary.advancement_criteria;

import com.google.gson.*;
import de.dafuqs.revelationary.*;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.*;
import net.minecraft.predicate.*;
import net.minecraft.predicate.entity.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;

import java.util.*;

public class AdvancementCountCriterion extends AbstractCriterion<AdvancementCountCriterion.Conditions> {
	
	public static final Identifier ID = new Identifier(Revelationary.MOD_ID, "advancement_count");
	
	public static AdvancementCountCriterion.Conditions create(Collection<Identifier> advancementIdentifiers, NumberRange.IntRange range) {
		return new AdvancementCountCriterion.Conditions(LootContextPredicate.EMPTY, advancementIdentifiers, range);
	}
	
	public Identifier getId() {
		return ID;
	}
	
	public AdvancementCountCriterion.Conditions conditionsFromAdvancementIdentifiers(LootContextPredicate lootContextPredicate, Collection<Identifier> advancementIdentifiers, NumberRange.IntRange range) {
		return new AdvancementCountCriterion.Conditions(lootContextPredicate, advancementIdentifiers, range);
	}
	
	public AdvancementCountCriterion.Conditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
		Collection<Identifier> advancementIdentifiers = new ArrayList<>();
		for (JsonElement jsonElement : jsonObject.getAsJsonArray("advancement_identifiers")) {
			advancementIdentifiers.add(Identifier.tryParse(jsonElement.getAsString()));
		}
		NumberRange.IntRange range = NumberRange.IntRange.fromJson(jsonObject.get("count"));
		return new AdvancementCountCriterion.Conditions(lootContextPredicate, advancementIdentifiers, range);
	}
	
	public void trigger(ServerPlayerEntity player) {
		this.trigger(player, (conditions) -> conditions.matches(player));
	}
	
	public static class Conditions extends AbstractCriterionConditions {
		private final Collection<Identifier> advancementIdentifiers;
		private final NumberRange.IntRange range;
		
		public Conditions(LootContextPredicate lootContextPredicate, Collection<Identifier> advancementIdentifiers, NumberRange.IntRange range) {
			super(ID, lootContextPredicate);
			this.advancementIdentifiers = advancementIdentifiers;
			this.range = range;
		}
		
		public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
			JsonObject jsonObject = super.toJson(predicateSerializer);
			JsonArray jsonArray = new JsonArray();
			for(Identifier id : this.advancementIdentifiers) {
				jsonArray.add(id.toString());
			}
			jsonObject.add("advancement_identifiers", jsonArray);
			jsonObject.addProperty("count", this.range.toString());
			return jsonObject;
		}
		
		public boolean matches(ServerPlayerEntity serverPlayerEntity) {
			ServerAdvancementLoader loader = serverPlayerEntity.server.getAdvancementLoader();
			if(loader == null) {
				return false;
			}
			PlayerAdvancementTracker tracker = serverPlayerEntity.getAdvancementTracker();
			if(tracker == null) {
				return false;
			}
			
			int matchingAdvancements = 0;
			boolean allMatched = true;
			for(Identifier advancementIdentifier : this.advancementIdentifiers) {
				Advancement advancement = loader.get(advancementIdentifier);
				if(advancement != null && tracker.getProgress(advancement).isDone()) {
					matchingAdvancements++;
				} else {
					allMatched = false;
				}
			}
			
			return this.range == null ? allMatched : this.range.test(matchingAdvancements);
		}
		
		public Collection<Identifier> getAdvancementIdentifiers() {
			return this.advancementIdentifiers;
		}
		
		public NumberRange.IntRange getRange() {
			return this.range;
		}
		
	}
	
}
