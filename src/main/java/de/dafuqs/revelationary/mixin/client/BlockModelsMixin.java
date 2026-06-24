package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.fabricmc.fabric.api.client.renderer.v1.model.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.world.level.block.state.*;
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
	private void revelationary$getModel(BlockState state, CallbackInfoReturnable<BlockStateModel> callbackInfoReturnable) {
		if (ClientRevelationHolder.isCloaked(state)) {
			BlockState destinationBlockState = ClientRevelationHolder.getCloakTarget(state);
			BlockStateModel overriddenModel = this.modelByState.getOrDefault(destinationBlockState, this.missingModel);
			callbackInfoReturnable.setReturnValue(overriddenModel);
		}
	}
}
