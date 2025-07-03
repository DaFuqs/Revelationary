package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.world.level.block.state.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BlockModelShaper.class)
public class BlockModelsMixin {
	@Shadow
	private Map<BlockState, BakedModel> modelByStateCache;
	
	@Shadow
	@Final
	private ModelManager modelManager;
	
	@Inject(at = @At("HEAD"), method = "getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;", cancellable = true)
	private void revelationary$getModel(BlockState blockState, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
		if (ClientRevelationHolder.isCloaked(blockState)) {
			BlockState destinationBlockState = ClientRevelationHolder.getCloakTarget(blockState);
			BakedModel overriddenModel = this.modelByStateCache.getOrDefault(destinationBlockState, modelManager.getMissingModel());
			callbackInfoReturnable.setReturnValue(overriddenModel);
		}
	}
}
