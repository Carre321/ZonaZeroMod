package com.zonazeromc.zzkits.gui;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.data.KitDefinition;
import com.zonazeromc.zzkits.data.KitSlotItem;
import com.zonazeromc.zzkits.data.UsageManager;
import com.zonazeromc.zzkits.listeners.PlayerReadyListener;
import com.zonazeromc.zzkits.util.CombatUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI de kits (Custom UI).
 *
 * - Izquierda: lista de kits (por defecto SOLO kits a los que el jugador tenga permiso kit.<nombre>)
 * - Derecha: preview completo (Inventario + Hotbar + Armor + Utility + Tools + Backpack) con scroll
 * - Botones: Reclamar / Cerrar
 */
public class KitGuiPage extends InteractiveCustomUIPage<KitGuiPageEventData> {

    private static final String LAYOUT = "ZZKitsMenu.ui";

    private static final String LIST_ID = "#KitList";

    private static final String LABEL_KIT_NAME = "#KitNameLabel.Text";
    private static final String LABEL_KIT_INFO = "#KitInfoLabel.Text";
    private static final String LABEL_KIT_STATUS = "#KitStatusLabel.Text";

    private static final String BTN_CLAIM = "#ClaimBtn";
    private static final String BTN_CLOSE = "#CloseBtn";

    private static final String SLOTS_STORAGE = "#StorageSlots";
    private static final String SLOTS_HOTBAR = "#HotbarSlots";
    private static final String SLOTS_ARMOR = "#ArmorSlots";
    private static final String SLOTS_UTILITY = "#UtilitySlots";
    private static final String SLOTS_TOOLS = "#ToolsSlots";
    private static final String SLOTS_BACKPACK = "#BackpackSlots";

    private final List<String> visibleKits = new ArrayList<>();
    private String selectedKit = null;

    private int capStorage = 0;
    private int capHotbar = 0;
    private int capArmor = 0;
    private int capUtility = 0;
    private int capTools = 0;
    private int capBackpack = 0;

    public KitGuiPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, KitGuiPageEventData.CODEC);
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> playerEntityRef,
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull UIEventBuilder eventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append(LAYOUT);

        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) return;

        // Construye los grids de slots una sola vez (según el inventario real del jugador)
        buildSlotGrids(player, commandBuilder);

        // Bind botones
        bindStaticButtons(eventBuilder);

        // Lista de kits
        rebuildKitList(playerEntityRef, store, commandBuilder, eventBuilder);

        // Estado inicial
        commandBuilder.set(LABEL_KIT_NAME, "Selecciona un kit");
        commandBuilder.set(LABEL_KIT_INFO, "");
        commandBuilder.set(LABEL_KIT_STATUS, "");

        // Asegura preview vacío
        clearAllSlotPreviews(commandBuilder);
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> playerEntityRef,
        @Nonnull Store<EntityStore> store,
        @Nonnull KitGuiPageEventData data
    ) {
        if (data == null) return;

        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) return;

        String type = (data.getType() == null) ? "" : data.getType();

        UICommandBuilder cb = new UICommandBuilder();
        UIEventBuilder eb = new UIEventBuilder();

        // Siempre re-bindea botones (por seguridad)
        bindStaticButtons(eb);

        if ("Close".equalsIgnoreCase(type)) {
            player.getPageManager().setPage(playerEntityRef, store, Page.None);
            return;
        }

        if ("Select".equalsIgnoreCase(type)) {
            if (data.getKit() != null && !data.getKit().isEmpty()) {
                this.selectedKit = ZZKits.instance().getKitManager().sanitize(data.getKit());
            }
            renderSelected(playerEntityRef, store, player, cb);
            rebuildKitList(playerEntityRef, store, cb, eb);
            sendUpdate(cb, eb, false);
            return;
        }

        if ("Claim".equalsIgnoreCase(type)) {
            if (selectedKit == null || selectedKit.isEmpty()) {
                ZZKits.instance().getMensajes().send(player, "gui_selecciona_kit", null);
            } else {
                attemptClaim(playerEntityRef, store, player, selectedKit);
            }
            // refrescar UI
            renderSelected(playerEntityRef, store, player, cb);
            rebuildKitList(playerEntityRef, store, cb, eb);
            sendUpdate(cb, eb, false);
            return;
        }

        // Compatibilidad: si no viene Type, se interpreta como click en kit (Select)
        if (data.getKit() != null && !data.getKit().isEmpty()) {
            this.selectedKit = ZZKits.instance().getKitManager().sanitize(data.getKit());
            renderSelected(playerEntityRef, store, player, cb);
            rebuildKitList(playerEntityRef, store, cb, eb);
            sendUpdate(cb, eb, false);
        }
    }

    private void bindStaticButtons(UIEventBuilder eb) {
        eb.addEventBinding(
            CustomUIEventBindingType.Activating,
            BTN_CLAIM,
            new EventData().append(KitGuiPageEventData.KEY_TYPE, "Claim")
        );
        eb.addEventBinding(
            CustomUIEventBindingType.Activating,
            BTN_CLOSE,
            new EventData().append(KitGuiPageEventData.KEY_TYPE, "Close")
        );
    }

    private void rebuildKitList(
        Ref<EntityStore> playerEntityRef,
        Store<EntityStore> store,
        UICommandBuilder cb,
        UIEventBuilder eb
    ) {
        visibleKits.clear();
        cb.clear(LIST_ID);

        var plugin = ZZKits.instance();

        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        boolean isAdmin = player != null && player.hasPermission("kit.admin");

        List<String> all = plugin.getKitManager().listKitNames();
        for (String kit : all) {
            String safe = plugin.getKitManager().sanitize(kit);

            // Por defecto: solo mostrar kits que el usuario puede usar (kit.<nombre>), admin ve todos
            if (!isAdmin && player != null && !player.hasPermission("kit." + safe)) {
                continue;
            }

            visibleKits.add(safe);
        }

        if (visibleKits.isEmpty()) {
            cb.append(LIST_ID, "ZZKits_InfoLabel.ui");
            cb.set(LIST_ID + "[0].Text", "No tienes kits disponibles.");
            return;
        }

        for (int i = 0; i < visibleKits.size(); i++) {
            String kit = visibleKits.get(i);

            cb.append(LIST_ID, "ZZKits_KitEntry.ui");
            cb.set(LIST_ID + "[" + i + "].Text", kit);

            // Click => Select
            eb.addEventBinding(
                CustomUIEventBindingType.Activating,
                LIST_ID + "[" + i + "]",
                new EventData()
                    .append(KitGuiPageEventData.KEY_KIT, kit)
                    .append(KitGuiPageEventData.KEY_TYPE, "Select")
            );
        }
    }

    private void buildSlotGrids(Player player, UICommandBuilder cb) {
        Inventory inv = player.getInventory();
        if (inv == null) return;

        capStorage = getCap(inv.getStorage());
        capHotbar = getCap(inv.getHotbar());
        capArmor = getCap(inv.getArmor());
        capUtility = 0; // Utility deshabilitado (no se usa en ZZKits)
        capTools = 0;   // Tools deshabilitado (no se usa en ZZKits)
        capBackpack = getCap(inv.getBackpack());

        buildGrid(cb, SLOTS_STORAGE, capStorage);
        buildGrid(cb, SLOTS_HOTBAR, capHotbar);
        buildGrid(cb, SLOTS_ARMOR, capArmor);
        buildGrid(cb, SLOTS_BACKPACK, capBackpack);
    }

    private int getCap(ItemContainer c) {
        if (c == null) return 0;
        return (int) c.getCapacity();
    }

    private void buildGrid(UICommandBuilder cb, String containerId, int cap) {
        cb.clear(containerId);
        for (int i = 0; i < cap; i++) {
            cb.append(containerId, "ZZKits_Slot48.ui");
        }
    }

    private void renderSelected(
        Ref<EntityStore> playerEntityRef,
        Store<EntityStore> store,
        Player player,
        UICommandBuilder cb
    ) {
        if (selectedKit == null || selectedKit.isEmpty()) {
            cb.set(LABEL_KIT_NAME, "Selecciona un kit");
            cb.set(LABEL_KIT_INFO, "");
            cb.set(LABEL_KIT_STATUS, "");
            clearAllSlotPreviews(cb);
            return;
        }

        var plugin = ZZKits.instance();
        KitDefinition kit = plugin.getKitManager().getKit(selectedKit);
        if (kit == null) {
            cb.set(LABEL_KIT_NAME, selectedKit);
            cb.set(LABEL_KIT_INFO, "Este kit no existe.");
            cb.set(LABEL_KIT_STATUS, "");
            clearAllSlotPreviews(cb);
            return;
        }

        cb.set(LABEL_KIT_NAME, kit.name);

        int totalStacks = countTotalStacks(kit);
        int totalQty = countTotalQuantity(kit);

        String cooldownTxt = (kit.cooldownSeconds <= 0) ? "sin cooldown" : (kit.cooldownSeconds + "s");
        String oneTimeTxt = kit.oneTime ? "sí" : "no";

        cb.set(LABEL_KIT_INFO,
            "Cooldown: " + cooldownTxt + "\n" +
            "1 solo uso: " + oneTimeTxt + "\n" +
            "Items: " + totalStacks + " stacks (" + totalQty + " unidades)"
        );

        cb.set(LABEL_KIT_STATUS, computeStatusText(store, playerEntityRef, player, kit, selectedKit));

        // Preview items
        renderKitPreview(cb, kit);
    }

    private String computeStatusText(Store<EntityStore> store, Ref<EntityStore> playerEntityRef, Player player, KitDefinition kit, String kitName) {
        if (player == null || kit == null) return "";

        boolean isAdmin = player.hasPermission("kit.admin");
        var cfg = ZZKits.instance().getConfigManager().get();

        // anti-combat
        if (cfg.preventKitsInCombat && !isAdmin) {
            long left = CombatUtil.secondsLeftInCombat(store, playerEntityRef, cfg.combatTagSeconds);
            if (left > 0) return "En combate: espera " + left + "s";
        }

        @SuppressWarnings("deprecation")
        UUID uuid = player.getUuid();

        UsageManager usage = ZZKits.instance().getUsageManager();

        if (!isAdmin) {
            if (kit.oneTime && usage.hasEverUsed(kitName, uuid)) return "Ya usado (1 solo uso)";

            if (kit.cooldownSeconds > 0) {
                long last = usage.getLastUsed(kitName, uuid);
                if (last > 0) {
                    long now = System.currentTimeMillis();
                    long remaining = (last + kit.cooldownSeconds * 1000L) - now;
                    if (remaining > 0) {
                        long secs = (remaining + 999) / 1000;
                        return "Cooldown: " + secs + "s";
                    }
                }
            }
        }

        return "Disponible";
    }

    private void clearAllSlotPreviews(UICommandBuilder cb) {
        clearSlots(cb, SLOTS_STORAGE, capStorage);
        clearSlots(cb, SLOTS_HOTBAR, capHotbar);
        clearSlots(cb, SLOTS_ARMOR, capArmor);
        clearSlots(cb, SLOTS_UTILITY, capUtility);
        clearSlots(cb, SLOTS_TOOLS, capTools);
        clearSlots(cb, SLOTS_BACKPACK, capBackpack);
    }

    private void clearSlots(UICommandBuilder cb, String base, int cap) {
        for (int i = 0; i < cap; i++) {
            // Vaciar slot
            cb.set(base + "[" + i + "].ItemId", "");
            cb.set(base + "[" + i + "].Quantity", 0);
        }
    }

    private void renderKitPreview(UICommandBuilder cb, KitDefinition kit) {
        clearAllSlotPreviews(cb);
        if (kit == null || kit.sections == null) return;

        applySectionToPreview(cb, kit.sections.get("storage"), SLOTS_STORAGE, capStorage);
        applySectionToPreview(cb, kit.sections.get("hotbar"), SLOTS_HOTBAR, capHotbar);
        applySectionToPreview(cb, kit.sections.get("armor"), SLOTS_ARMOR, capArmor);
        applySectionToPreview(cb, java.util.Collections.emptyList(), SLOTS_UTILITY, capUtility);
        applySectionToPreview(cb, java.util.Collections.emptyList(), SLOTS_TOOLS, capTools);
        applySectionToPreview(cb, kit.sections.get("backpack"), SLOTS_BACKPACK, capBackpack);
    }

    private void applySectionToPreview(UICommandBuilder cb, List<KitSlotItem> items, String base, int cap) {
        if (items == null || items.isEmpty()) return;
        for (KitSlotItem it : items) {
            int slot = it.slot();
            if (slot < 0 || slot >= cap) continue;
            cb.set(base + "[" + slot + "].ItemId", it.itemId());
            cb.set(base + "[" + slot + "].Quantity", Math.max(0, it.quantity()));
        }
    }

    private int countTotalStacks(KitDefinition kit) {
        int n = 0;
        if (kit == null || kit.sections == null) return 0;
        for (List<KitSlotItem> l : kit.sections.values()) {
            if (l != null) n += l.size();
        }
        return n;
    }

    private int countTotalQuantity(KitDefinition kit) {
        int n = 0;
        if (kit == null || kit.sections == null) return 0;
        for (List<KitSlotItem> l : kit.sections.values()) {
            if (l == null) continue;
            for (KitSlotItem i : l) {
                n += Math.max(0, i.quantity());
            }
        }
        return n;
    }

    private void attemptClaim(
        Ref<EntityStore> playerEntityRef,
        Store<EntityStore> store,
        Player player,
        String kitName
    ) {
        var plugin = ZZKits.instance();
        var cfg = plugin.getConfigManager().get();
        var mensajes = plugin.getMensajes();

        KitDefinition kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            mensajes.send(player, "kit_no_existe", Map.of("kit", kitName));
            return;
        }

        boolean isAdmin = player.hasPermission("kit.admin");
        if (!isAdmin && !player.hasPermission("kit." + kitName)) {
            mensajes.send(player, "no_permiso_kit", Map.of("kit", kitName));
            return;
        }

        // Anti-combat
        if (cfg.preventKitsInCombat && !isAdmin) {
            long left = CombatUtil.secondsLeftInCombat(store, playerEntityRef, cfg.combatTagSeconds);
            if (left > 0) {
                mensajes.send(player, "en_combate", Map.of("secs", String.valueOf(left)));
                return;
            }
        }

        UsageManager usage = plugin.getUsageManager();

        @SuppressWarnings("deprecation")
        UUID uuid = player.getUuid();

        if (!isAdmin) {
            if (kit.oneTime && usage.hasEverUsed(kitName, uuid)) {
                mensajes.send(player, "one_time_usado");
                return;
            }

            if (kit.cooldownSeconds > 0) {
                long last = usage.getLastUsed(kitName, uuid);
                if (last > 0) {
                    long now = System.currentTimeMillis();
                    long cdMs = kit.cooldownSeconds * 1000L;
                    long remaining = (last + cdMs) - now;
                    if (remaining > 0) {
                        long secs = (remaining + 999) / 1000;
                        mensajes.send(player, "cooldown", Map.of("secs", String.valueOf(secs)));
                        return;
                    }
                }
            }
        }

        boolean overwrite = cfg.overwriteSlotsOnGive;
        String fullMode = cfg.inventoryFullMode;

        PlayerReadyListener.GiveResult res =
            PlayerReadyListener.giveKitToPlayer(player, playerEntityRef, store, kit, overwrite, fullMode);

        if (!res.success()) {
            mensajes.send(player, "inventario_sin_espacio");
            return;
        }

        // Marcar uso SOLO si se entregó
        usage.markUsedNow(kitName, uuid);
        usage.save();

        mensajes.send(player, "kit_recibido", Map.of("kit", kitName));

        if (res.droppedQuantity() > 0) {
            mensajes.send(player, "items_tirados_suelo", Map.of("dropped", String.valueOf(res.droppedQuantity())));
        }
    }
}
