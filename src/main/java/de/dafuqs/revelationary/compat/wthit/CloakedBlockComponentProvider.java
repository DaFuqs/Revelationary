package de.dafuqs.revelationary.compat.wthit;

import de.dafuqs.revelationary.api.revelations.RevelationAware;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class CloakedBlockComponentProvider implements IBlockComponentProvider {
    @Override
    public @Nullable BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
        PlayerEntity player = accessor.getPlayer();

        RevelationAware aware = (RevelationAware) accessor.getBlock();
        if (!aware.isVisibleTo(player)) {
            return aware.getBlockStateCloaks().get(accessor.getBlockState());
        }

        return accessor.getBlockState();
    }
}
