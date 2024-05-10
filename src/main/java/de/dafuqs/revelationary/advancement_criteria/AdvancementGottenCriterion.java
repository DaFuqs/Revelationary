package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class AdvancementGottenCriterion extends AbstractCriterion<AdvancementGottenCriterion.Conditions> {
	public void trigger(ServerPlayerEntity player, AdvancementEntry advancement) {
		this.trigger(player, (conditions) -> conditions.matches(advancement));
	}

	@Override
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public record Conditions(Optional<LootContextPredicate> player, Identifier advancementIdentifier) implements AbstractCriterion.Conditions {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				Identifier.CODEC.fieldOf("advancement_identifier").forGetter(Conditions::advancementIdentifier))
			.apply(inst, Conditions::new));

		public boolean matches(AdvancementEntry advancement) {
			return this.advancementIdentifier.equals(advancement.id());
		}
		
		public Identifier getAdvancementIdentifier() {
			return advancementIdentifier;
		}
	}
}
