# ZZKits (Hytale Server Plugin)

Sistema de **kits** para servidores de Hytale: kits con permisos, cooldown / one-time, kit de inicio (starter) al primer join, comandos por consola y mensajes traducibles.

---

## ✅ Características

- **Múltiples kits** (`/kit create <nombre>`, `/kit delete <nombre>`)
- **Permisos por kit**: `kit.<nombreDelKit>` (ej: `kit.magofuego`)
- **Cooldown** por kit y opción **one-time** (1 solo uso)
- **Kit de inicio** automático al **primer join** (configurable)
- Captura y entrega:
  - **storage (inventario)**
  - **hotbar**
  - **equipado** (armor/tools/utility)
  - **backpack**
- **Comandos desde consola** (configurable)
- **GUI de kits**: `/kits` o `/kit gui`
- **Anti-combat**: bloquea reclamar kits durante X segundos tras pelear
- Mensajes en **`mensajes.json`** (traducibles) con colores usando `&`

---

## 📦 Instalación

1. Compila el plugin (ver “Compilación”).
2. Copia el JAR generado a la carpeta `mods/` del servidor.
3. Inicia el servidor.

Al arrancar, ZZKits crea:

- `mods/ZZKits/config.json`
- `mods/ZZKits/mensajes.json`
- `mods/ZZKits/kits/` (kits en JSON)
- `mods/ZZKits/usage.json` (cooldowns / one-time)

---

## ⌨️ Comandos

### Jugadores

- **`/kit list`** (alias: `/kit ls`)
  - Lista los kits existentes.

- **`/kit <kit>`**
  - Obtiene un kit.
  - Requiere permiso `kit.<kit>` (o `kit.admin`).

### Admin (`kit.admin`)

- **`/kit reload`**
  - Recarga `config.json`, `mensajes.json`, kits y `usage.json` sin reiniciar servidor.

- **`/kit get <kit> <jugador>`**
  - Entrega un kit a un jugador específico (forzado; no consume cooldown del jugador por “ser admin give”).

- **`/kit create <kit> [cooldownSeg] [oneTime] [jugador]`**
  - Crea/actualiza un kit capturando inventario/hotbar/equipado del jugador.
  - Si lo ejecutas como jugador, `[jugador]` es opcional.
  - Desde consola, **debes** indicar `[jugador]`.

  Ejemplos:
  - Jugador: `/kit create magofuego 3600 false`
  - Consola: `/kit create magofuego 3600 false Steve`

- **`/kit delete <kit>`**
  - Elimina el kit.

- **`/kitsetstarter [jugador]`** (alias: `/setkitstarter [jugador]`)
  - Guarda el inventario del jugador como kit starter (nombre definido en `config.json -> starterKit`).

---

## 🔐 Permisos (LuckPerms)

- **Admin total**: `kit.admin`
- **Uso de un kit**: `kit.<nombreKit>`

Ejemplo:
- Permitir `/kit magofuego`:
  - `kit.magofuego`

> Nota: los comandos básicos `/kit` y `/kit list` no requieren permiso.

---

## ⚙️ Configuración (`config.json`)

Archivo: `mods/ZZKits/config.json`

Opciones principales:

- `prefix`: prefijo del plugin (usa `&` para colores)
- `starterKit`: nombre del kit starter
- `giveStarterOnFirstJoin`: si entrega starter en primer join
- `overwriteSlotsOnGive`: si sobrescribe slots (hotbar/armor/etc) al entregar
- `defaultCooldownSeconds`: cooldown por defecto al crear kits
- `defaultOneTime`: one-time por defecto al crear kits
- `inventoryFullMode`: qué hacer si el inventario está lleno al entregar un kit:
  - `"DROP"`: lo que no quepa se **tira al suelo**
  - `"CANCEL"`: **no entrega nada** y avisa
- `allowConsoleCommands`: permitir comandos desde consola

Opciones de GUI:
- `enableGui`: habilita `/kits` y `/kit gui`
- `guiClickAction`: `"CLAIM"` (reclamar con click) o `"SELECT"` (solo seleccionar)
- `guiShowLockedKits`: si muestra kits sin permiso

Opciones Anti-Combat:
- `preventKitsInCombat`: bloquea reclamar kits mientras estás en combate
- `combatTagSeconds`: segundos de bloqueo desde la última acción de combate

### Inventario lleno (modo CANCEL)
En `"CANCEL"` el plugin hace un chequeo **estricto**:
- Si el jugador no tiene **mínimo tantos huecos libres como stacks** tenga el kit, **NO se entrega**.
- No consume cooldown / one-time si no se entrega.

---

## 🗂️ Formato de kits (`mods/ZZKits/kits/<kit>.json`)

Cada kit guarda secciones:
- `storage`, `hotbar`, `armor`, `tools`, `utility`, `backpack`

Cada entrada guarda el `itemId`, `quantity`, `durability` y `slot` (si aplica).

---

## 💬 Mensajes (`mensajes.json`)

Archivo: `mods/ZZKits/mensajes.json`

- Colores con `&` (estilo Minecraft).
- Placeholders típicos:
  - `{prefix}`, `{kit}`, `{player}`, `{secs}`, `{count}`, `{dropped}`

Después de cambiar mensajes, usa:
- `/kit reload`

---

## 🧪 Flujo recomendado

1. Entra como admin y prepara tu inventario/hotbar/equipamiento.
2. Crea un kit:
   - `/kit create pvp 1800 false`
3. Da permisos en LuckPerms:
   - `kit.pvp`
4. Prueba como usuario:
   - `/kit pvp`
5. Configura starter (opcional):
   - `/kitsetstarter`
   - `starterKit: "default"` (o el que quieras)
