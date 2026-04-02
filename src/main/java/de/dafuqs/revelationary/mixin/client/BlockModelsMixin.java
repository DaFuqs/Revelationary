package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelSet;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BlockStateModelSet.class)
public class BlockModelsMixin implements FabricBlockStateModelSet {
	
	
	@Final
    @Shadow
	private Map<BlockState, BlockStateModel> modelByState;
	
	@Shadow
	@Final
	private BlockStateModel missingModel;
	
	@Inject(at = @At("HEAD"), method = "get", cancellable = true)
	private void revelationary$getModel(BlockState blockState, CallbackInfoReturnable<BlockStateModel> callbackInfoReturnable) {
		if (ClientRevelationHolder.isCloaked(blockState)) {
			BlockState destinationBlockState = ClientRevelationHolder.getCloakTarget(blockState);
			BlockStateModel overriddenModel = this.modelByState.getOrDefault(destinationBlockState, this.missingModel);
			callbackInfoReturnable.setReturnValue(overriddenModel);
		}
	}
}
