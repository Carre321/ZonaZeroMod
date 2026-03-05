package com.zonazeromc.zzkits.util;

import com.zonazeromc.zzkits.data.KitSlotItem;
import com.hypixel.hytale.server.core.inventory.ItemStack;

/**
 * Conversión KitSlotItem <-> ItemStack.
 *
 * IMPORTANTE: actualmente guardamos metadata como null (igual que StarterKitPlus).
 */
public final class ItemStackUtil {

    private ItemStackUtil() {}

    public static ItemStack toItemStack(KitSlotItem item) {
        return new ItemStack(
            item.itemId(),
            item.quantity(),
            item.durability(),
            item.maxDurability(),
            null
        );
    }
}
