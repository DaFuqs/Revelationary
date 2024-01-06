package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.config.*;
import net.fabricmc.loader.api.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.*;

public final class Plugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(mixinClassName.contains("BlockUnbreakingMixin")) {
			return RevelationaryConfig.get().PreventMiningOfUnrevealedBlocks;
		}
		if(mixinClassName.contains("DrawContextMixin")){
			return FabricLoader.getInstance().isModLoaded("connectormod");
		}
		if(mixinClassName.contains("ItemModelsMixin")){
			return !FabricLoader.getInstance().isModLoaded("connectormod");
		}
		return true;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}
	
	@Override
	public List<String> getMixins() {
		return List.of();
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	
}