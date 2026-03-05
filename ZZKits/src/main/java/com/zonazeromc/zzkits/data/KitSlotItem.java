package com.zonazeromc.zzkits.data;

/**
 * Un item dentro de un contenedor con su slot (para hotbar/armor/etc).
 */
public record KitSlotItem(
    int slot,
    String itemId,
    int quantity,
    double durability,
    double maxDurability
) {}
