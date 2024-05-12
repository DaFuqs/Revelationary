package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.ClientAdvancements;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientAdvancementManager.class)
public abstract class ClientAdvancementManagerMixin {
	/**
	 * Intercepts advancement packets sent from server to client
	 * When new advancements are added ClientAdvancements is triggered
	 * resulting in updating block visibility in the world (ModelSwapper)
	 *
	 * @param packet The vanilla advancement packet
	 * @param info   Mixin callback info
	 */
	@Inject(at = @At("RETURN"), method = "onAdvancements")
	public void revelationary$onAdvancementSync(AdvancementUpdateS2CPacket packet, CallbackInfo info) {
		ClientAdvancements.onClientPacket(packet);
	}
}