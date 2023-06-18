package de.dafuqs.revelationary.advancement_criteria;

import com.google.gson.JsonObject;
import de.dafuqs.revelationary.Revelationary;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class HadRevelationCriterion extends AbstractCriterion<HadRevelationCriterion.Conditions> {
	
	public static final Identifier ID = new Identifier(Revelationary.MOD_ID, "had_revelation");
	
	public static HadRevelationCriterion.Conditions create(Identifier id) {
		return new HadRevelationCriterion.Conditions(LootContextPredicate.EMPTY, id);
	}
	
	public Identifier getId() {
		return ID;
	}
	
	public HadRevelationCriterion.Conditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
		Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "revelation_identifier"));
		return new HadRevelationCriterion.Conditions(lootContextPredicate, identifier);
	}
	
	public void trigger(ServerPlayerEntity player, Block block) {
		this.trigger(player, (conditions) -> {
			return conditions.matches(block);
		});
	}
	
	public static class Conditions extends AbstractCriterionConditions {
		private final Identifier identifier;
		
		public Conditions(LootContextPredicate lootContextPredicate, Identifier identifier) {
			super(ID, lootContextPredicate);
			this.identifier = identifier;
		}
		
		public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
			JsonObject jsonObject = super.toJson(predicateSerializer);
			jsonObject.addProperty("revelation_identifier", this.identifier.toString());
			return jsonObject;
		}
		
		public boolean matches(Object object) {
			if (identifier.getPath().isEmpty()) {
				// if "revelation_identifier": "" => trigger with any revelation
				return true;
			} else if (object instanceof Block cloakableBlock) {
				return Registries.BLOCK.getId(cloakableBlock).equals(identifier);
			} else if (object instanceof Item cloakableItem) {
				return Registries.ITEM.getId(cloakableItem).equals(identifier);
			} else {
				return false;
			}
		}
	}
	
}
