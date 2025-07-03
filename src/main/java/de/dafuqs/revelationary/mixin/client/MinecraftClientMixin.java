package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Minecraft;disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V")
	public void revelationary$onLogout(Screen nextScreen, boolean keepResourcePacks, CallbackInfo ci) {
		ClientAdvancements.playerLogout();
	}
}