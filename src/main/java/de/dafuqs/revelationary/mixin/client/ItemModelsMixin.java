package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.client.render.item.*;
import net.minecraft.client.render.model.*;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(BakedModel.class)
public abstract class ItemModelsMixin {
 
	@Shadow
    public abstract BakedModel getModel(ItemStack stack);

    @Inject(at = @At("HEAD"), method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;", cancellable = true)
    
    private void revelationary$getModel(ItemStack itemStack, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
        if (ClientRevelationHolder.isCloaked(itemStack.getItem())) {
            Item destinationItem = ClientRevelationHolder.getCloakTarget(itemStack.getItem());
            BakedModel overriddenModel = getModel(destinationItem.getDefaultStack());
            callbackInfoReturnable.setReturnValue(overriddenModel);
        }
    }
	
}
