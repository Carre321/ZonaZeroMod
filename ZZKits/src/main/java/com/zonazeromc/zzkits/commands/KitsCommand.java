package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.zonazeromc.zzkits.gui.KitGuiPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /kits [player]
 * Alias "directo" para abrir el menú de kits.
 */
public class KitsCommand extends AbstractKitTargetCommand {

    public KitsCommand() {
        super("kits", "Abre el menú (GUI) de kits");
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
        // Si un jugador intenta abrir GUI a otro, exigir admin
        if (senderRef != null && !senderRef.equals(targetRef) && !ctx.sender().hasPermission("kit.admin")) {
            ZZKits.instance().getMensajes().send(ctx, "no_puedes_dar_otro");
            return;
        }

        Player target = store.getComponent(targetRef, Player.getComponentType());
        if (target == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return;
        }

        target.getPageManager().openCustomPage(targetRef, store, new KitGuiPage(target.getPlayerRef()));
        ZZKits.instance().getMensajes().send(ctx, "gui_abierto");
    }
}
