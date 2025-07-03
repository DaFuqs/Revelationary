package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ItemModelShaper.class)
public abstract class ItemModelsMixin {
    @Shadow
    public abstract BakedModel getItemModel(ItemStack stack);
    
    @Inject(at = @At("HEAD"), method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", cancellable = true)
    private void revelationary$getItemModel(ItemStack itemStack, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
        if (ClientRevelationHolder.isCloaked(itemStack.getItem())) {
            Item destinationItem = ClientRevelationHolder.getCloakTarget(itemStack.getItem());
            BakedModel overriddenModel = getItemModel(destinationItem.getDefaultInstance());
            callbackInfoReturnable.setReturnValue(overriddenModel);
        }
    }
}
