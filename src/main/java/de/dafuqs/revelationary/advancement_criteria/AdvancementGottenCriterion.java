package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
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
	
	public record Conditions(Optional<ContextAwarePredicate> player,
							 ResourceLocation advancementIdentifier) implements SimpleInstance {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
						ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
						ResourceLocation.CODEC.fieldOf("advancement_identifier").forGetter(Conditions::advancementIdentifier))
			.apply(inst, Conditions::new));
		
		public boolean matches(AdvancementHolder advancement) {
			return this.advancementIdentifier.equals(advancement.id());
		}
		
		public ResourceLocation getAdvancementIdentifier() {
			return advancementIdentifier;
		}
	}
}
