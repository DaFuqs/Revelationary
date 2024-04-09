package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class HadRevelationCriterion extends AbstractCriterion<HadRevelationCriterion.Conditions> {
	
	public void trigger(ServerPlayerEntity player, Block block) {
		this.trigger(player, (conditions) -> conditions.matches(block));
	}

	@Override
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public record Conditions(Optional<LootContextPredicate> player, Identifier identifier) implements AbstractCriterion.Conditions {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				Identifier.CODEC.fieldOf("revelation_identifier").forGetter(Conditions::identifier))
			.apply(inst, Conditions::new));
		
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
