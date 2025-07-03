package de.dafuqs.revelationary.compat.rei;

import de.dafuqs.revelationary.api.revelations.*;
import de.dafuqs.revelationary.config.*;
import me.shedaniel.rei.api.client.entry.filtering.base.*;
import me.shedaniel.rei.api.client.plugins.*;
import me.shedaniel.rei.api.common.util.*;
import net.minecraft.world.item.*;

import java.util.*;
import java.util.stream.*;


public class RevelationaryREIPlugin implements REIClientPlugin {
    @SuppressWarnings("UnstableApiUsage")
    private BasicFilteringRule.MarkDirty filteringRule;
    private static Set<Item> hiddenStacks = Set.of();

    public RevelationaryREIPlugin() {
        if (!RevelationaryConfig.get().HideCloakedEntriesFromRecipeViewers) return;
        CloakSetChanged.EVENT.register((added, removed, newStacks) -> {
            hiddenStacks = newStacks;
            //noinspection UnstableApiUsage
            filteringRule.markDirty();
        });
    }

    @Override
    public void registerBasicEntryFiltering(@SuppressWarnings("UnstableApiUsage") BasicFilteringRule<?> rule) {
        // not using .show to not interfere with other filtering rules
        if (!RevelationaryConfig.get().HideCloakedEntriesFromRecipeViewers) return;
        //noinspection UnstableApiUsage
        filteringRule = rule.hide(() ->
            hiddenStacks.stream()
                    .map(EntryStacks::of)
                    .collect(Collectors.toList())
        );
    }
}
