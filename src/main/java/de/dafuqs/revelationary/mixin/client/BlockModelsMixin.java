package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.fabricmc.fabric.api.renderer.v1.model.*;
import net.minecraft.block.*;
import net.minecraft.client.render.block.*;
import net.minecraft.client.render.model.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BlockModels.class)
public class BlockModelsMixin implements FabricBlockModels {
	
	
	@Shadow
	private Map<BlockState, BlockStateModel> models;
	
	@Shadow
	@Final
	private BakedModelManager modelManager;
	
	@Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
	private void revelationary$getModel(BlockState blockState, CallbackInfoReturnable<BlockStateModel> callbackInfoReturnable) {
		if (ClientRevelationHolder.isCloaked(blockState)) {
			BlockState destinationBlockState = ClientRevelationHolder.getCloakTarget(blockState);
			BlockStateModel overriddenModel = this.models.getOrDefault(destinationBlockState, this.modelManager.getMissingModel());
			callbackInfoReturnable.setReturnValue(overriddenModel);
		}
	}
}
