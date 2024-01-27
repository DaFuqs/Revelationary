package de.dafuqs.revelationary.compat.jade;

import de.dafuqs.revelationary.api.revelations.RevelationAware;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

public class RevelationaryJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            if (accessor instanceof BlockAccessor blockAccessor) {
                PlayerEntity player = accessor.getPlayer();
                if (player.isCreative() || player.isSpectator()) {
                    return accessor;
                }

                if (blockAccessor.getBlock() instanceof RevelationAware aware) {
                    if (!aware.isVisibleTo(player)) {
                        BlockState cloakedState = aware.getBlockStateCloaks().get(blockAccessor.getBlockState());
                        return registration.blockAccessor().from(blockAccessor).blockState(cloakedState).build();
                    }
                }
            }

            return accessor;
        });
    }
}
