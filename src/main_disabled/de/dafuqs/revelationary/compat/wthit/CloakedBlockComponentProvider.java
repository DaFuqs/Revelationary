package de.dafuqs.revelationary.compat.wthit;

import de.dafuqs.revelationary.api.revelations.*;
import mcp.mobius.waila.api.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.*;
import org.jetbrains.annotations.*;

public class CloakedBlockComponentProvider implements IBlockComponentProvider {
    @Override
    public @Nullable BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
        Player player = accessor.getPlayer();

        RevelationAware aware = (RevelationAware) accessor.getBlock();
        if (!aware.isVisibleTo(player)) {
            return aware.getBlockStateCloaks().get(accessor.getBlockState());
        }

        return accessor.getBlockState();
    }
}
