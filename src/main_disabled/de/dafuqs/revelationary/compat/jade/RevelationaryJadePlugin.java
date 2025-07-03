package de.dafuqs.revelationary.compat.jade;

import de.dafuqs.revelationary.api.revelations.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.*;
import snownee.jade.api.*;

public class RevelationaryJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            if (accessor instanceof BlockAccessor blockAccessor) {
                Player player = accessor.getPlayer();
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
