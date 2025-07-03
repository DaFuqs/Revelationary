package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(net.minecraft.client.multiplayer.ClientAdvancements.class)
public abstract class ClientAdvancementManagerMixin {
	/**
	 * Intercepts advancement packets sent from server to client
	 * When new advancements are added ClientAdvancements is triggered
	 * resulting in updating block visibility in the world (ModelSwapper)
	 *
	 * @param packet The vanilla advancement packet
	 * @param info   Mixin callback info
	 */
	@Inject(at = @At("RETURN"), method = "update(Lnet/minecraft/network/protocol/game/ClientboundUpdateAdvancementsPacket;)V")
	public void revelationary$onAdvancementSync(ClientboundUpdateAdvancementsPacket packet, CallbackInfo info) {
		ClientAdvancements.onClientPacket(packet);
	}
}