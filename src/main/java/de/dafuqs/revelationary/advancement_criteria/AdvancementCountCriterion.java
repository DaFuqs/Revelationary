package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.*;
import net.minecraft.predicate.*;
import net.minecraft.predicate.entity.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;

import java.util.*;

public class AdvancementCountCriterion extends AbstractCriterion<AdvancementCountCriterion.Conditions> {

	public void trigger(ServerPlayerEntity player) {
		this.trigger(player, (conditions) -> conditions.matches(player));
	}

	@Override
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public record Conditions(Optional<LootContextPredicate> player, List<Identifier> advancementIdentifiers, NumberRange.IntRange range) implements AbstractCriterion.Conditions {

		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				Identifier.CODEC.listOf().fieldOf("advancement_identifiers").forGetter(Conditions::advancementIdentifiers),
				NumberRange.IntRange.CODEC.fieldOf("count").forGetter(Conditions::range))
			.apply(inst, Conditions::new));

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
				AdvancementEntry advancement = loader.get(advancementIdentifier);
				if(advancement != null && tracker.getProgress(advancement).isDone()) {
					matchingAdvancements++;
				} else {
					allMatched = false;
				}
			}
			
			return this.range == null ? allMatched : this.range.test(matchingAdvancements);
		}
	}
	
}
