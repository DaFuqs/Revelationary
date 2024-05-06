package de.dafuqs.revelationary.api.revelations;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;

import java.util.Set;

@FunctionalInterface
public interface CloakSetChanged {
    Event<CloakSetChanged> EVENT = EventFactory.createArrayBacked(CloakSetChanged.class,
            (listeners) -> (addedCloaks, removedCloaks, newCloaks) -> {
                for (CloakSetChanged listener : listeners) listener.onChange(addedCloaks, removedCloaks, newCloaks);
            }
            );
    // the diffs matter for JEI, the new cloaks set matters for REI
    void onChange(Set<Item> addedCloaks, Set<Item> removedCloaks, Set<Item> newCloaks);
}
