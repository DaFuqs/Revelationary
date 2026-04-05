package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.util.*;

import java.util.*;

public class AdvancementCountCriterion extends SimpleCriterionTrigger<AdvancementCountCriterion.Conditions> {
	public void trigger(ServerPlayer player) {
		this.trigger(player, (conditions) -> conditions.matches(player));
	}

	@Override
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}

	public record Conditions(Optional<ContextAwarePredicate> player, List<Identifier> advancementIdentifiers, MinMaxBounds.Ints range) implements SimpleCriterionTrigger.SimpleInstance {

		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				Identifier.CODEC.listOf().fieldOf("advancement_identifiers").forGetter(Conditions::advancementIdentifiers),
				MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(Conditions::range))
			.apply(inst, Conditions::new));

		public boolean matches(ServerPlayer serverPlayerEntity) {
			ServerAdvancementManager loader = serverPlayerEntity.level().getServer().getAdvancements();
			if(loader == null) {
				return false;
			}
			PlayerAdvancements tracker = serverPlayerEntity.getAdvancements();
			if(tracker == null) {
				return false;
			}
			
			int matchingAdvancements = 0;
			boolean allMatched = true;
			for(Identifier advancementIdentifier : this.advancementIdentifiers) {
				AdvancementHolder advancement = loader.get(advancementIdentifier);
				if(advancement != null && tracker.getOrStartProgress(advancement).isDone()) {
					matchingAdvancements++;
				} else {
					allMatched = false;
				}
			}
			
			return this.range == null ? allMatched : this.range.matches(matchingAdvancements);
		}
	}
}
