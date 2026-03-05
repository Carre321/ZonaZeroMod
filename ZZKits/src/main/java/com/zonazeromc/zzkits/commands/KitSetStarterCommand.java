package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.data.ConfigData;
import com.zonazeromc.zzkits.data.KitDefinition;
import com.zonazeromc.zzkits.data.KitManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /kitsetstarter [player]
 * Guarda el inventario/hotbar/equipo del jugador como kit "starterKit" del config.json.
 */
public class KitSetStarterCommand extends AbstractKitTargetCommand {

    public KitSetStarterCommand() {
        super("kitsetstarter", "Guarda tu inventario actual como el kit starter");

        // [player] al final
        enableTargetPlayerArg();
    }

    @Override
    protected void executeTarget(
        @Nonnull CommandContext ctx,
        Ref<EntityStore> senderRef,
        @Nonnull Ref<EntityStore> targetRef,
        @Nonnull World world,
        @Nonnull Store<EntityStore> store
    ) {
        if (!ctx.sender().hasPermission("kit.admin")) {
            ZZKits.instance().getMensajes().send(ctx, "no_permiso_admin");
            return;
        }

        Player target = store.getComponent(targetRef, Player.getComponentType());
        if (target == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return;
        }

        Inventory inv = target.getInventory();
        if (inv == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_inventario");
            return;
        }

        ConfigData cfg = ZZKits.instance().getConfigManager().get();
        KitManager kitManager = ZZKits.instance().getKitManager();

        String starter = kitManager.sanitize(cfg.starterKit == null ? "default" : cfg.starterKit);

        try {
            KitDefinition def = kitManager.snapshotFromInventory(starter, inv, 0, false);
            kitManager.saveKit(def);
            var mensajes = ZZKits.instance().getMensajes();
            mensajes.send(ctx, "starter_guardado", java.util.Map.of("kit", starter));
            mensajes.send(ctx, "starter_info");
ZZKits.LOGGER.atInfo().log("[ZZKits] starterKit '%s' actualizado por %s.", starter, ctx.sender().getDisplayName());
        } catch (Exception e) {
            ZZKits.instance().getMensajes().send(ctx, "error_guardando_starter", java.util.Map.of("error", String.valueOf(e.getMessage())));
        }
    }
}
