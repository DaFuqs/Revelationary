package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.level.block.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Block.class)
public class BlockClientMixin {
	@Inject(method = "getName()Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
	private void revelationary$getCloakedName(CallbackInfoReturnable<MutableComponent> callbackInfoReturnable) {
		Block thisBlock = (Block) (Object) this;
		if (ClientRevelationHolder.isCloaked(thisBlock)) {
			callbackInfoReturnable.setReturnValue(RevelationRegistry.getTranslationString(thisBlock));
		}
	}
}
