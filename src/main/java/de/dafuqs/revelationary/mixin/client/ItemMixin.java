package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Item.class)
public abstract class ItemMixin {
	@Inject(at = @At("HEAD"), method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;", cancellable = true)
	public void revelationary$getCloakedName(ItemStack stack, CallbackInfoReturnable<Component> callbackInfoReturnable) {
		Item thisItem = (Item) (Object) this;
		if (ClientRevelationHolder.isCloaked(thisItem)) {
			callbackInfoReturnable.setReturnValue(RevelationRegistry.getTranslationString(thisItem));
		}
	}
}
