package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.ClientRevelationHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
	
	@Shadow
	@Final
	private Int2ObjectMap<BakedModel> models;
	
	@Shadow
	@Final
	private BakedModelManager modelManager;
	
	@Inject(at = @At("HEAD"), method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;", cancellable = true)
	private void revelationary$getModel(ItemStack itemStack, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
		if (ClientRevelationHolder.isCloaked(itemStack.getItem())) {
			Item destinationItem = ClientRevelationHolder.getCloakTarget(itemStack.getItem());
			BakedModel overriddenModel = this.models.getOrDefault(Item.getRawId(destinationItem), modelManager.getMissingModel());
			callbackInfoReturnable.setReturnValue(overriddenModel);
		}
	}
	
}
