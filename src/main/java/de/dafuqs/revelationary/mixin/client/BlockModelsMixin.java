package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.ClientRevelationHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockModels.class)
public class BlockModelsMixin {
	
	@Shadow
	private Map<BlockState, BakedModel> models;
	
	@Shadow
	@Final
	private BakedModelManager modelManager;
	
	@Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
	private void getModel(BlockState blockState, CallbackInfoReturnable<BakedModel> callbackInfoReturnable) {
		if (ClientRevelationHolder.isCloaked(blockState)) {
			BlockState destinationBlockState = ClientRevelationHolder.getCloakTarget(blockState);
			BakedModel overriddenModel = this.models.getOrDefault(destinationBlockState, modelManager.getMissingModel());
			callbackInfoReturnable.setReturnValue(overriddenModel);
		}
	}
	
}
