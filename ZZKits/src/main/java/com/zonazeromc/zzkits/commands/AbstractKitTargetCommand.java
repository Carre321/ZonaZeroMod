package com.zonazeromc.zzkits.commands;

import com.zonazeromc.zzkits.ZZKits;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import com.hypixel.hytale.server.core.command.system.CommandOwner;

/**
 * Base para comandos que pueden apuntar a un jugador:
 * - Si el ejecutor es jugador y no se especifica [player], el target es él mismo.
 * - Si lo ejecuta consola, se requiere [player].
 *
 * Importante: NO aplica el check automático de permisos ".other" de AbstractTargetPlayerCommand,
 * porque ZZKits usa permisos propios (kit.admin, kit.<kitName>).
 */
public abstract class AbstractKitTargetCommand extends AbstractAsyncCommand {

    protected OptionalArg<PlayerRef> playerArg;

    protected AbstractKitTargetCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Constructor para usage variants (addUsageVariant): NO deben tener nombre.
     */
    protected AbstractKitTargetCommand(String description) {
        super(description);
    }


    @Override
    public void setOwner(CommandOwner owner) {
        super.setOwner(owner);
        // Quita cualquier permiso auto-generado del framework para variants/target commands.
        requirePermission(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        // Desactiva el permiso automático del motor (lo maneja ZZKits manualmente).
        return false;
    }


    /**
     * Activa el argumento opcional [player]. Llamar al FINAL del constructor del comando
     * para que aparezca como el último argumento en el usage.
     */
    protected final void enableTargetPlayerArg() {
        if (playerArg != null) return;
        this.playerArg = withOptionalArg("player", "Jugador objetivo", ArgTypes.PLAYER_REF);
    }

    @Override
    protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {

        // Config de consola
        if (!ctx.isPlayer() && !ZZKits.instance().getConfigManager().get().allowConsoleCommands) {
            ZZKits.instance().getMensajes().send(ctx, "consola_desactivada");
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> senderRef = ctx.isPlayer() ? ctx.senderAsPlayerRef() : null;

        Ref<EntityStore> targetRef;
        if (playerArg != null && playerArg.provided(ctx)) {
            PlayerRef pr = playerArg.get(ctx);
            targetRef = (pr != null) ? pr.getReference() : null;
        } else {
            targetRef = senderRef;
        }

        if (targetRef == null) {
            ZZKits.instance().getMensajes().send(ctx, "target_requerido");
            return CompletableFuture.completedFuture(null);
        }

        if (!targetRef.isValid()) {
            ZZKits.instance().getMensajes().send(ctx, "target_ref_invalida");
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = targetRef.getStore();
        if (store == null || store.getExternalData() == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return CompletableFuture.completedFuture(null);
        }

        if (!(store.getExternalData() instanceof EntityStore entityStore)) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return CompletableFuture.completedFuture(null);
        }

        World world = entityStore.getWorld();
        if (world == null) {
            ZZKits.instance().getMensajes().send(ctx, "error_objetivo");
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(ctx, () -> executeTarget(ctx, senderRef, targetRef, world, store), world);
    }

    protected abstract void executeTarget(
        @Nonnull CommandContext ctx,
        Ref<EntityStore> senderRef,
        @Nonnull Ref<EntityStore> targetRef,
        @Nonnull World world,
        @Nonnull Store<EntityStore> store
    );
}
