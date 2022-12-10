package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.networking.RevelationaryS2CPacketSenders;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
	
	@Inject(method = "onPlayerConnect", at = @At(value = "RETURN"))
	private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		RevelationaryS2CPacketSenders.sendRevelations(player);
	}
	
}
