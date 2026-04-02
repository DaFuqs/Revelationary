package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementGottenCriterion extends SimpleCriterionTrigger<AdvancementGottenCriterion.Conditions> {
	public void trigger(ServerPlayer player, AdvancementHolder advancement) {
		this.trigger(player, (conditions) -> conditions.matches(advancement));
	}

	@Override
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}

	public record Conditions(Optional<ContextAwarePredicate> player, Identifier advancementIdentifier) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				Identifier.CODEC.fieldOf("advancement_identifier").forGetter(Conditions::advancementIdentifier))
			.apply(inst, Conditions::new));

		public boolean matches(AdvancementHolder advancement) {
			return this.advancementIdentifier.equals(advancement.id());
		}
		
		public Identifier getAdvancementIdentifier() {
			return advancementIdentifier;
		}
	}
}
