package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.ClientRevelationHolder;
import de.dafuqs.revelationary.RevelationRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Item.class)
public abstract class ItemMixin {
	
	@Inject(at = @At("HEAD"), method = "getName(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/text/Text;", cancellable = true)
	public void getCloakedName(ItemStack stack, CallbackInfoReturnable<Text> callbackInfoReturnable) {
		Item thisItem = (Item) (Object) this;
		if (ClientRevelationHolder.isCloaked(thisItem)) {
			callbackInfoReturnable.setReturnValue(RevelationRegistry.getTranslationString(thisItem));
		}
	}
	
}
