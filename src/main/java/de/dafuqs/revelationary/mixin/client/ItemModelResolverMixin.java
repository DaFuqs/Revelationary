package de.dafuqs.revelationary.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.dafuqs.revelationary.ClientRevelationHolder;
import net.fabricmc.api.*;
import net.minecraft.client.renderer.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @SuppressWarnings("LocalMayUseName")
    @Inject(method = "appendItemLayers", at = @At("HEAD"))
    private void cloakItem(ItemStackRenderState output, ItemStack item, ItemDisplayContext displayContext, Level level, ItemOwner owner, int seed, CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) LocalRef<ItemStack> itemRef) {
        // TODO: figure out if not cloaking any items with non-default models is a good idea
        // TODO: possibly extra config for this?
        if (item.hasNonDefault(DataComponents.ITEM_MODEL))
            return;
        Item source = item.getItem();
        Item target = ClientRevelationHolder.getCloakTarget(source);
        if (target != source)
            itemRef.set(item.transmuteCopy(target));
    }
}
