package de.dafuqs.revelationary.mixin.client;

import net.minecraft.client.render.model.*;
import org.spongepowered.asm.mixin.*;

@Mixin(BakedModel.class)
public abstract class ItemModelsMixin {
    /*@Shadow
    public abstract BakedModel getModel(ItemStack stack);

    @Inject(at = @At("HEAD"), method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;", cancellable = true)
    ItemRenderer
    
    private void revelationary$getModel(ItemStack itemStack, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
        if (ClientRevelationHolder.isCloaked(itemStack.getItem())) {
            Item destinationItem = ClientRevelationHolder.getCloakTarget(itemStack.getItem());
            BakedModel overriddenModel = getModel(destinationItem.getDefaultStack());
            callbackInfoReturnable.setReturnValue(overriddenModel);
        }
    }*/
}
