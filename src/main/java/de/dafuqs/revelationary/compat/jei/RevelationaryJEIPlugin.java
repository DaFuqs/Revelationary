package de.dafuqs.revelationary.compat.jei;

import de.dafuqs.revelationary.Revelationary;
import de.dafuqs.revelationary.api.revelations.CloakSetChanged;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
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
        CloakSetChanged.EVENT.register((added, removed, newStacks) -> {
            stacksCache = newStacks;
            if (runtime != null) {
                var manager = runtime.getIngredientManager();
                manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                        added.stream().map(ItemStack::new).collect(Collectors.toList()));
                manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                        removed.stream().map(ItemStack::new).collect(Collectors.toList()));
            }
        });
    }

    @Override
    public @NotNull Identifier getPluginUid() {
        return new Identifier(Revelationary.MOD_ID);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (stacksCache != null) runtime.getIngredientManager()
                .removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                        stacksCache.stream().map(ItemStack::new).collect(Collectors.toList()));
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
