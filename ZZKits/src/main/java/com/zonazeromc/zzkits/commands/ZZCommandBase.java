package com.zonazeromc.zzkits.commands;

import com.hypixel.hytale.server.core.command.system.CommandOwner;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

/**
 * Base de comandos de ZZKits que DESACTIVA la generación automática de permisos del motor.
 *
 * En el sistema de comandos de Hytale, si no desactivas esto, el motor genera un permiso automático
 * (normalmente basado en el nombre completo del comando) y bloquea el uso para jugadores sin ese permiso.
 *
 * ZZKits maneja permisos manualmente:
 * - kit.admin
 * - kit.<nombreKit>
 */
public abstract class ZZCommandBase extends CommandBase {

    public ZZCommandBase(String name, String description) {
        super(name, description);
    }

    public ZZCommandBase(String name, String description, boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);
    }

    /** Constructor "solo descripción" (para usage variants). */
    public ZZCommandBase(String description) {
        super(description);
    }


    @Override
    public void setOwner(CommandOwner owner) {
        super.setOwner(owner);
        // Asegura que no exista un permiso auto-generado a nivel de framework.
        // Los permisos reales los maneja ZZKits (kit.admin, kit.<kit>).
        requirePermission(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        // Sin permiso automático => cualquiera puede ejecutar el comando y luego ZZKits decide con hasPermission().
        return false;
    }
}