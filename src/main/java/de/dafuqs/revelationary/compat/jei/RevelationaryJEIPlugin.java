package de.dafuqs.revelationary.compat.jei;

import de.dafuqs.revelationary.Revelationary;
import de.dafuqs.revelationary.api.revelations.CloakSetChanged;
import de.dafuqs.revelationary.config.RevelationaryConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.loader.api.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class RevelationaryJEIPlugin implements IModPlugin {
    private IJeiRuntime runtime;
    private Set<Item> stacksCache;

    public RevelationaryJEIPlugin() {
        if (!RevelationaryConfig.get().HideCloakedEntriesFromRecipeViewers) return;
        
        // While EMI does implement the JEIModPlugin
        // It only handles addIngredientsAtRuntime / removeIngredientsAtRuntime
        // Once on join, making uncloaked items not show up at all, unless
        // the player leaves & rejoins the world
        if(FabricLoader.getInstance().isModLoaded("emi")) {
            return;
        }
        
        CloakSetChanged.EVENT.register((added, removed, newStacks) -> {
            stacksCache = newStacks;
            if (runtime != null) {
                var manager = runtime.getIngredientManager();
                if(added != null && !added.isEmpty())
                    manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                            added.stream().map(ItemStack::new).collect(Collectors.toList()));
                if(removed != null && !removed.isEmpty())
                    manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                            removed.stream().map(ItemStack::new).collect(Collectors.toList()));
            }
        });
    }

    @Override
    public @NotNull Identifier getPluginUid() {
        return new Identifier(Revelationary.MOD_ID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (!RevelationaryConfig.get().HideCloakedEntriesFromRecipeViewers) return;
        if (stacksCache != null && !stacksCache.isEmpty()) runtime.getIngredientManager()
                .removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                        stacksCache.stream().map(ItemStack::new).collect(Collectors.toList()));
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
