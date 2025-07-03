package de.dafuqs.revelationary.api.revelations;

import net.minecraft.client.*;
import net.minecraft.world.item.*;

import java.util.*;

@FunctionalInterface
public interface CloakSetChanged {
    Event<CloakSetChanged> EVENT = EventFactory.createArrayBacked(CloakSetChanged.class,
            (listeners) -> (addedCloaks, removedCloaks, newCloaks) -> {
                Minecraft.getInstance().execute(() -> {
                    for (CloakSetChanged listener : listeners) listener.onChange(addedCloaks, removedCloaks, newCloaks);
                });
            });
    // the diffs matter for JEI, the new cloaks set matters for REI
    void onChange(Set<Item> addedCloaks, Set<Item> removedCloaks, Set<Item> newCloaks);
}
