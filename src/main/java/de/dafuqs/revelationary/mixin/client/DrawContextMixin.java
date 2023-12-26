package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.ClientRevelationHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DrawContext.class)
public class DrawContextMixin {

    @ModifyVariable(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ItemStack revelationary$drawItem(ItemStack stack){
        if (!ClientRevelationHolder.isCloaked(stack.getItem())) return stack;
        Item destinationItem = ClientRevelationHolder.getCloakTarget(stack.getItem());
        return destinationItem.getDefaultStack();
    }

}
