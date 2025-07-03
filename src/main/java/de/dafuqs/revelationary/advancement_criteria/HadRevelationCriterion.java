package de.dafuqs.revelationary.advancement_criteria;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

import java.util.*;

public class HadRevelationCriterion extends SimpleCriterionTrigger<HadRevelationCriterion.Conditions> {
	public void trigger(ServerPlayer player, Block block) {
		this.trigger(player, (conditions) -> conditions.matches(block));
	}

	@Override
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}
	
	public record Conditions(Optional<ContextAwarePredicate> player,
							 ResourceLocation identifier) implements SimpleInstance {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
						ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
						ResourceLocation.CODEC.fieldOf("revelation_identifier").forGetter(Conditions::identifier))
			.apply(inst, Conditions::new));
		
		public boolean matches(Object object) {
			if (identifier.getPath().isEmpty()) {
				// if "revelation_identifier": "" => trigger with any revelation
				return true;
			} else if (object instanceof Block cloakableBlock) {
				return BuiltInRegistries.BLOCK.getKey(cloakableBlock).equals(identifier);
			} else if (object instanceof Item cloakableItem) {
				return BuiltInRegistries.ITEM.getKey(cloakableItem).equals(identifier);
			} else {
				return false;
			}
		}
	}
}
