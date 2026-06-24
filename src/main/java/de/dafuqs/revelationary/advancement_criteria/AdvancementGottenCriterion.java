package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.advancements.*;
import net.minecraft.advancements.predicates.*;
import net.minecraft.advancements.triggers.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;

import java.util.*;

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
