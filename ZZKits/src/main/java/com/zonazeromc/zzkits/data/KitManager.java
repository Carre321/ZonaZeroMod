package com.zonazeromc.zzkits.data;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.util.JsonUtil;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Gestiona kits en /kits/*.json
 * Además migra el formato viejo de StarterKitPlus:
 *  - kits/default.txt
 *  - received_players.txt
 */
public class KitManager {

    private final File kitsDir;
    private final ConfigManager configManager;

    private final Map<String, KitDefinition> kits = new HashMap<>();

    public KitManager(Path dataDirectory, ConfigManager configManager) {
        this.kitsDir = dataDirectory.resolve("kits").toFile();
        this.kitsDir.mkdirs();
        this.configManager = configManager;
    }

    public synchronized void loadAll() {
        kits.clear();
        File[] files = kitsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) return;

        for (File f : files) {
            try {
                KitDefinition def = JsonUtil.readJson(f, KitDefinition.class, null);
                if (def == null || def.name == null || def.name.isEmpty()) continue;
                kits.put(def.name.toLowerCase(), def);
            } catch (Exception ignored) {}
        }
    }

    public synchronized List<String> listKitNames() {
        List<String> names = new ArrayList<>(kits.keySet());
        Collections.sort(names);
        return names;
    }

    public synchronized KitDefinition getKit(String name) {
        if (name == null) return null;
        return kits.get(name.toLowerCase());
    }

    public synchronized boolean exists(String name) {
        return getKit(name) != null;
    }

    public synchronized void saveKit(KitDefinition def) throws IOException {
        if (def == null || def.name == null || def.name.isEmpty()) return;
        String safeName = sanitize(def.name);
        def.name = safeName;
        File file = new File(kitsDir, safeName.toLowerCase() + ".json");
        JsonUtil.writeJson(file, def);
        kits.put(safeName.toLowerCase(), def);
    }

    public synchronized boolean deleteKit(String name) {
        String safe = sanitize(name);
        File file = new File(kitsDir, safe.toLowerCase() + ".json");
        kits.remove(safe.toLowerCase());
        return file.exists() && file.delete();
    }

    public File getKitsDir() { return kitsDir; }

    public String sanitize(String name) {
        if (name == null) return "default";
        String s = name.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^a-z0-9_\\-]", "");
        if (s.isEmpty()) s = "default";
        return s;
    }

    public KitDefinition snapshotFromInventory(String kitName, Inventory inv, int cooldownSeconds, boolean oneTime) {
        KitDefinition def = new KitDefinition();
        def.name = sanitize(kitName);
        def.cooldownSeconds = Math.max(0, cooldownSeconds);
        def.oneTime = oneTime;

        def.sections.put("storage", snapshotSection(inv.getStorage()));
        def.sections.put("hotbar", snapshotSection(inv.getHotbar()));
        def.sections.put("armor", snapshotSection(inv.getArmor()));
        // Nota: utility/tools suelen contener herramientas del modo creativo/servidor.
        // Para evitar que se cuelen en kits, NO se guardan por defecto.
        def.sections.put("utility", java.util.Collections.emptyList());
        def.sections.put("tools", java.util.Collections.emptyList());
        def.sections.put("backpack", snapshotSection(inv.getBackpack()));

        return def;
    }

    private List<KitSlotItem> snapshotSection(ItemContainer container) {
        if (container == null) return Collections.emptyList();
        List<KitSlotItem> items = new ArrayList<>();
        short cap = container.getCapacity();
        for (short slot = 0; slot < cap; slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack == null) continue;
            String id = stack.getItemId();
            if (id == null || id.isEmpty()) continue;
            int qty = stack.getQuantity();
            if (qty <= 0) continue;

            items.add(new KitSlotItem(
                slot,
                id,
                qty,
                stack.getDurability(),
                stack.getMaxDurability()
            ));
        }
        return items;
    }

    /**
     * Migra datos viejos del plugin StarterKitPlus (.txt -> .json).
     * - Si existe kits/default.txt y no existe kits/default.json => crea default.json
     * - Si existe received_players.txt => los marca como usados para starterKit (config.starterKit)
     */
    public void migrateLegacyIfNeeded(UsageManager usageManager) {
        try {
            migrateLegacyKitTxt();
            migrateLegacyReceivedPlayers(usageManager);
        } catch (Exception e) {
            ZZKits.LOGGER.atWarning().log("[ZZKits] Error en migración legacy: %s", e.getMessage());
        }
    }

    private void migrateLegacyKitTxt() throws IOException {
        File legacy = new File(kitsDir, "default.txt");
        File target = new File(kitsDir, "default.json");
        if (!legacy.exists() || target.exists()) return;

        List<KitSlotItem> storage = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(legacy))) {
            String line;
            int slot = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length != 4) continue;

                String itemId = parts[0];
                int qty = Integer.parseInt(parts[1]);
                double dur = Double.parseDouble(parts[2]);
                double max = Double.parseDouble(parts[3]);

                storage.add(new KitSlotItem(slot, itemId, qty, dur, max));
                slot++;
            }
        } catch (Exception ignored) {}

        KitDefinition def = new KitDefinition();
        def.name = "default";
        def.cooldownSeconds = 0;
        def.oneTime = false;
        def.sections.put("storage", storage);

        saveKit(def);

        // No borramos el txt automáticamente para no sorprender al admin
    }

    private void migrateLegacyReceivedPlayers(UsageManager usageManager) {
        File legacy = kitsDir.getParentFile() != null
            ? new File(kitsDir.getParentFile(), "received_players.txt")
            : null;
        if (legacy == null || !legacy.exists()) return;

        String starter = configManager.get().starterKit != null ? configManager.get().starterKit : "default";
        starter = sanitize(starter);

        try (BufferedReader reader = new BufferedReader(new FileReader(legacy))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    UUID uuid = UUID.fromString(line);
                    usageManager.markUsedNow(starter, uuid);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // No borramos el txt automáticamente
    }
}
