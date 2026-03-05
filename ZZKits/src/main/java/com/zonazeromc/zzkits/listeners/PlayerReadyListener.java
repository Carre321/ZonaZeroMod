package com.zonazeromc.zzkits.listeners;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.data.ConfigData;
import com.zonazeromc.zzkits.data.KitDefinition;
import com.zonazeromc.zzkits.data.UsageManager;
import com.zonazeromc.zzkits.util.ItemStackUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.UUID;

public final class PlayerReadyListener {

    private PlayerReadyListener() {}

    /** Resultado de entregar un kit */
    public record GiveResult(boolean success, int processedStacks, int droppedQuantity, boolean cancelledNoSpace) {}

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        @SuppressWarnings("deprecation")
        UUID uuid = player.getUuid();

        ZZKits plugin = ZZKits.instance();
        ConfigData cfg = plugin.getConfigManager().get();

        if (!cfg.giveStarterOnFirstJoin) return;

        String starterKit = (cfg.starterKit == null || cfg.starterKit.isEmpty()) ? "default" : cfg.starterKit;
        starterKit = plugin.getKitManager().sanitize(starterKit);

        UsageManager usage = plugin.getUsageManager();

        // Si ya lo recibió alguna vez, no repetir en join
        if (usage.hasEverUsed(starterKit, uuid)) return;

        KitDefinition kit = plugin.getKitManager().getKit(starterKit);
        if (kit == null) {
            plugin.LOGGER.atWarning().log("[ZZKits] starterKit='%s' no existe. Usa /kit create %s o /kitsetstarter.", starterKit, starterKit);
            return;
        }

        boolean overwrite = cfg.overwriteSlotsOnGive;

        // En PlayerReadyEvent no tenemos un Store<EntityStore> accesible aquí.
        // Si config pide DROP pero no podemos tirar al suelo, hacemos CANCEL por seguridad.
        GiveResult res = giveKitToPlayer(player, event.getPlayerRef(), null, kit, overwrite, cfg.inventoryFullMode);

        if (!res.success()) {
            // No marcar como usado si no se entregó
            plugin.getMensajes().send(player, "inventario_sin_espacio");
            return;
        }

        usage.markUsedNow(starterKit, uuid);
        usage.save();

        plugin.getMensajes().send(player, "starter_recibido_join", java.util.Map.of(
            "kit", starterKit,
            "count", String.valueOf(res.processedStacks())
        ));

        if (res.droppedQuantity() > 0) {
            plugin.getMensajes().send(player, "items_tirados_suelo", java.util.Map.of(
                "dropped", String.valueOf(res.droppedQuantity())
            ));
        }

        plugin.LOGGER.atInfo().log("[ZZKits] Kit '%s' entregado en join a UUID=%s (%d stacks, dropped=%d).", starterKit, uuid, res.processedStacks(), res.droppedQuantity());
    }

    /**
     * Entrega el kit respetando slots (hotbar/armor/etc).
     *
     * inventoryFullMode:
     *  - "DROP"   -> lo que no quepa se tira al suelo (si es posible)
     *  - "CANCEL" -> si no hay espacio suficiente, NO entrega nada
     */
    public static GiveResult giveKitToPlayer(Player player, Ref<EntityStore> playerRef, Store<EntityStore> store, KitDefinition kit, boolean overwriteSlots, String inventoryFullMode) {
        if (player == null || kit == null) return new GiveResult(false, 0, 0, false);

        Inventory inv = player.getInventory();
        if (inv == null) return new GiveResult(false, 0, 0, false);

        ItemContainer fallback = inv.getCombinedBackpackStorageHotbarFirst();

        String mode = (inventoryFullMode == null || inventoryFullMode.isEmpty()) ? "DROP" : inventoryFullMode.trim();
        boolean canDrop = (playerRef != null && store != null);
        boolean dropMode = mode.equalsIgnoreCase("DROP") && canDrop;
        boolean cancelMode = mode.equalsIgnoreCase("CANCEL") || (mode.equalsIgnoreCase("DROP") && !canDrop);

        // Si estamos en CANCEL, comprobar espacio ANTES de tocar el inventario.
// Modo estricto: si no hay mínimo tantos huecos como stacks a entregar, NO se entrega nada.
        if (cancelMode) {
            if (!hasSpaceForKitCancelStrict(inv, kit, overwriteSlots)) {
                return new GiveResult(false, 0, 0, true);
            }
        }

java.util.function.Consumer<ItemStack> dropper = null;
        if (dropMode) {
            dropper = (stack) -> {
                try {
                    ItemUtils.dropItem(playerRef, stack, store);
                } catch (Exception ignored) {}
            };
        }

        int processed = 0;
        int droppedQty = 0;

        ApplyResult r;

        r = applySection(inv.getStorage(), kit.sections.get("storage"), fallback, overwriteSlots, dropper);
        processed += r.processedStacks; droppedQty += r.droppedQuantity;

        r = applySection(inv.getHotbar(), kit.sections.get("hotbar"), fallback, overwriteSlots, dropper);
        processed += r.processedStacks; droppedQty += r.droppedQuantity;

        r = applySection(inv.getArmor(), kit.sections.get("armor"), fallback, overwriteSlots, dropper);
        processed += r.processedStacks; droppedQty += r.droppedQuantity;
        // No aplicamos utility/tools (evita herramientas creativas/system en kits)

        r = applySection(inv.getBackpack(), kit.sections.get("backpack"), fallback, overwriteSlots, dropper);
        processed += r.processedStacks; droppedQty += r.droppedQuantity;

        return new GiveResult(true, processed, droppedQty, false);
    }

    /** Resultado interno por sección */
    private record ApplyResult(int processedStacks, int droppedQuantity) {}

    private static ApplyResult applySection(ItemContainer container, List<com.zonazeromc.zzkits.data.KitSlotItem> items, ItemContainer fallback, boolean overwriteSlots, java.util.function.Consumer<ItemStack> dropper) {
        if (container == null || items == null || items.isEmpty()) return new ApplyResult(0, 0);
        int processed = 0;
        int droppedQty = 0;

        for (com.zonazeromc.zzkits.data.KitSlotItem kitItem : items) {
            try {
                ItemStack stack = ItemStackUtil.toItemStack(kitItem);

                boolean placed = false;
                int slot = kitItem.slot();
                if (slot >= 0 && slot < container.getCapacity()) {
                    ItemStack existing = container.getItemStack((short) slot);
                    boolean empty = existing == null || existing.getItemId() == null || existing.getItemId().isEmpty() || existing.getQuantity() <= 0;
                    if (overwriteSlots || empty) {
                        container.setItemStackForSlot((short) slot, stack);
                        placed = true;
                    }
                }

                if (!placed) {
                    ItemContainer target = (fallback != null) ? fallback : container;
                    ItemStackTransaction tx = target.addItemStack(stack);
                    ItemStack rem = (tx != null) ? tx.getRemainder() : null;
                    if (rem != null && rem.getQuantity() > 0) {
                        if (dropper != null) {
                            droppedQty += rem.getQuantity();
                            dropper.accept(rem);
                        }
                        // Si dropper es null, aquí no hacemos nada (no debería pasar en DROP porque se convierte a CANCEL)
                    }
                }

                processed++;
            } catch (Exception ignored) {}
        }

        return new ApplyResult(processed, droppedQty);
    }

    
    /**
     * Modo CANCEL (estricto):
     * - Si el jugador NO tiene mínimo tantos huecos libres como stacks necesita el kit, NO se entrega nada.
     * - El conteo considera que cada stack ocupa 1 hueco (no intenta optimizar por apilado).
     * - Además, descuenta los huecos que se van a ocupar por colocaciones en slots preferidos (hotbar/storage/backpack),
     *   para evitar entregas parciales (caso típico: 1 hueco libre -> solo entra 1 item).
     */
    private static boolean hasSpaceForKitCancelStrict(Inventory inv, KitDefinition kit, boolean overwriteSlots) {
        if (inv == null || kit == null) return false;

        ItemContainer storage = inv.getStorage();
        ItemContainer hotbar = inv.getHotbar();
        ItemContainer backpack = inv.getBackpack();
        ItemContainer armor = inv.getArmor();
        // utility/tools ignorados para kits (evita creative tools)
        ItemContainer utility = null;
        ItemContainer tools = null;

        int emptyStorage = countEmptySlots(storage);
        int emptyHotbar = countEmptySlots(hotbar);
        int emptyBackpack = countEmptySlots(backpack);

        // "fallback" real del plugin: storage + backpack + hotbar (en ese orden)
        int fallbackEmpty = emptyStorage + emptyBackpack + emptyHotbar;

        boolean[] usedStorage = makeUsed(storage);
        boolean[] usedHotbar = makeUsed(hotbar);
        boolean[] usedBackpack = makeUsed(backpack);
        boolean[] usedArmor = makeUsed(armor);

        // Simular consumo de huecos por sección en el mismo orden que se aplica el kit.
        fallbackEmpty = consumeSectionCancelStrict(storage, usedStorage, fallbackEmpty, kit.sections.get("storage"), overwriteSlots, true);
        if (fallbackEmpty < 0) return false;

        fallbackEmpty = consumeSectionCancelStrict(hotbar, usedHotbar, fallbackEmpty, kit.sections.get("hotbar"), overwriteSlots, true);
        if (fallbackEmpty < 0) return false;

        fallbackEmpty = consumeSectionCancelStrict(armor, usedArmor, fallbackEmpty, kit.sections.get("armor"), overwriteSlots, false);
        if (fallbackEmpty < 0) return false;
        // utility/tools ignorados

        fallbackEmpty = consumeSectionCancelStrict(backpack, usedBackpack, fallbackEmpty, kit.sections.get("backpack"), overwriteSlots, true);
        if (fallbackEmpty < 0) return false;

        return true;
    }

    private static int consumeSectionCancelStrict(
        ItemContainer container,
        boolean[] usedSlots,
        int fallbackEmpty,
        List<com.zonazeromc.zzkits.data.KitSlotItem> items,
        boolean overwriteSlots,
        boolean containerIsFallbackPool
    ) {
        if (items == null || items.isEmpty()) return fallbackEmpty;

        // Si no existe el contenedor, todo va a fallback
        if (container == null) {
            fallbackEmpty -= items.size();
            return fallbackEmpty;
        }

        int cap = container.getCapacity();

        for (com.zonazeromc.zzkits.data.KitSlotItem kitItem : items) {
            int slot = kitItem.slot();

            boolean placedPreferred = false;

            // ¿Tiene slot preferido válido?
            if (slot >= 0 && slot < cap) {
                // En overwriteSlots=true: siempre se puede colocar en el slot, aunque esté ocupado.
                // Pero si el slot estaba vacío, consume un hueco libre real.
                if (overwriteSlots) {
                    placedPreferred = true;

                    if (containerIsFallbackPool && isEmptySlot(container, usedSlots, slot)) {
                        markUsed(usedSlots, slot);
                        fallbackEmpty--;
                        if (fallbackEmpty < 0) return fallbackEmpty;
                    } else {
                        // Aunque no sea fallbackPool, marcamos used para reflejar que el slot deja de estar "vacío"
                        // y evitar dobles consumos si otro item vuelve a apuntar al mismo slot.
                        markUsed(usedSlots, slot);
                    }
                } else {
                    // overwriteSlots=false: solo se coloca si está vacío.
                    if (isEmptySlot(container, usedSlots, slot)) {
                        placedPreferred = true;
                        markUsed(usedSlots, slot);
                        if (containerIsFallbackPool) {
                            fallbackEmpty--;
                            if (fallbackEmpty < 0) return fallbackEmpty;
                        }
                    }
                }
            }

            if (!placedPreferred) {
                // Va al fallback (storage/backpack/hotbar)
                fallbackEmpty--;
                if (fallbackEmpty < 0) return fallbackEmpty;
            }
        }

        return fallbackEmpty;
    }

    private static int countEmptySlots(ItemContainer container) {
        if (container == null) return 0;
        int cap = container.getCapacity();
        int empty = 0;
        for (int i = 0; i < cap; i++) {
            ItemStack s = container.getItemStack((short) i);
            if (isEmpty(s)) empty++;
        }
        return empty;
    }

    private static boolean isEmptySlot(ItemContainer container, boolean[] usedSlots, int slot) {
        if (container == null) return false;
        if (slot < 0 || slot >= container.getCapacity()) return false;
        if (usedSlots != null && slot < usedSlots.length && usedSlots[slot]) return false;

        ItemStack s = container.getItemStack((short) slot);
        return isEmpty(s);
    }

    private static boolean isEmpty(ItemStack existing) {
        return existing == null
            || existing.getItemId() == null
            || existing.getItemId().isEmpty()
            || existing.getQuantity() <= 0;
    }

    private static boolean[] makeUsed(ItemContainer container) {
        if (container == null) return null;
        return new boolean[container.getCapacity()];
    }

    private static void markUsed(boolean[] usedSlots, int slot) {
        if (usedSlots == null) return;
        if (slot < 0 || slot >= usedSlots.length) return;
        usedSlots[slot] = true;
    }

}
