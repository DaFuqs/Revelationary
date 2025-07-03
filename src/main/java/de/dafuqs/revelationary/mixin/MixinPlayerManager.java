package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import net.minecraft.network.*;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.server.players.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(PlayerList.class)
public class MixinPlayerManager {
	@Inject(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V", at = @At(value = "RETURN"))
	private void revelationary$onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
		RevelationaryNetworking.sendRevelations(player);
	}
}
