# ZZDisconect 1.0.4

Plugin de administracion para servidores Hytale enfocado en:
- Paradas controladas con redireccion de jugadores.
- Modo mantenimiento con limpieza de jugadores no-op.
- Avisos globales con cooldown.
- Placeholders de estado.
- Reinicio automatico programable.
- Recarga en caliente de configuracion con `/zzdisconect reload`.

## Resumen de cambios recientes
1. Se elimino la dependencia local por `systemPath` y se usa Maven normal con repositorio de Hytale.
2. Se adapto el codigo a la API actual de Hytale (CommandContext/PlayerRef).
3. Se agrego comando administrativo `/zzdisconect reload` (alias raiz `/zzd`).
4. Se agrego reinicio seguro de tareas programadas al recargar config (placeholder refresh y auto-restart).

## Comandos disponibles (punto a punto)

### 1. `/parar <mensaje> <tiempoSegundos>`
- Alias: `/zzparar`
- Funcion: inicia la secuencia de apagado controlado sin `--confirm`.
- Ejemplo: `/zzparar Reinicio_por_actualizacion 30`
- Flujo:
  1. Marca el estado como `SHUTTING_DOWN`.
  2. Lanza broadcast inicial con: `El servidor se esta cerrando...`.
  3. Lanza broadcast automatico cada segundo con tiempo restante.
  4. Redirige jugadores por lotes al destino configurado.
  5. Espera `PostTransferDelayMs`.
  6. Si `StopServerAfterRedirect=true`, ejecuta `stop`.

### 2. `/cancelarparada --confirm`
- Alias: `/zzcancelarparada`
- Funcion: cancela una parada en curso (si todavia no entro en `REDIRECTING`).
- Resultado:
  1. Cancela tareas internas de parada.
  2. Limpia timer de shutdown.
  3. Vuelve a `ONLINE` o `MAINTENANCE` segun config.

### 3. `/halt --confirm`
- Alias: `/zzhalt`
- Funcion: entra en mantenimiento sin apagar el servidor.
- Flujo:
  1. Cambia estado a `MAINTENANCE`.
  2. Anuncia mantenimiento.
  3. Redirige jugadores no-op.
  4. Activa whitelist si `HaltEnableWhitelist=true`.

### 4. `/move <jugador> <host> <port> --confirm`
- Alias: `/zzmove`
- Funcion: mueve un jugador especifico a un server puntual.
- Validaciones:
  1. Jugador valido/conectado.
  2. Host/port validos.

### 5. `/aviso "mensaje" [segundos|static]`
- Alias: `/avisos`, `/zzaviso`, `/zzavisos`
- Funcion: envia anuncio centrado en pantalla (no chat).
- Reglas:
  1. Respeta `AvisosEnabled`.
  2. Respeta cooldown (`AvisosCooldownSeconds`).
  3. Respeta largo maximo (`AvisosMaxLength`).
  4. Puede aplicar placeholders (`AvisosAllowPlaceholders`).
  5. Soporta modo estatico hasta reinicio con `static`.
- Ejemplos:
  1. `/aviso "Mensaje de prueba"`
  2. `/aviso "Mensaje de prueba" 10`
  3. `/aviso "Mantenimiento activo" static`
  4. `/aviso off` (desactiva el aviso estatico actual)

### 6. `/lobbyinfo`
- Alias: `/zzlobby`
- Funcion: muestra jugadores conectados y estado interno del plugin.

### 7. `/zzdisconect reload`
- Alias raiz: `/zzd reload`
- Funcion: recarga config sin reiniciar el servidor.
- Que recarga exactamente:
  1. Lee `config` desde disco (`config.load().join()`).
  2. Actualiza estado a `ONLINE`/`MAINTENANCE` si no hay parada en curso.
  3. Refresca cache de placeholders.
  4. Reinicia tarea de refresco de placeholders con el nuevo intervalo.
  5. Cancela tareas viejas de auto-restart y reprograma segun nueva config.
- Que NO hace:
  1. No vuelve a registrar comandos ni eventos.
  2. No reinicia el scheduler del plugin.
  3. No interrumpe una secuencia de parada ya activa.

## Nota sobre `--confirm`
En este plugin `parar` ya no requiere `--confirm`.

Siguen requiriendo `--confirm`:
- `cancelarparada`
- `halt`
- `move`

## Configuracion (bloques y claves principales)
El plugin usa una config tipada (`ZZDisconectConfig`) y crea/lee un archivo `config` en la carpeta de datos del plugin.

### 1. Identidad y meta
- `ModName`, `ConfigVersion`, `ModVersion`, `Debug`
- `ServerName`, `ServerId`
- `PlayerPrefix`, `ConsolePrefix`

### 2. Parada controlada (`/parar`)
- `PararEnabled`
- `PararDurationSeconds`
- `PararBroadcastIntervalsSeconds`
- `PostTransferDelayMs`
- `MarkOfflineAfterRedirect`
- `StopServerAfterRedirect`

### 3. Redireccion de jugadores
- `RedirectTargetHost`, `RedirectTargetPort`
- `FallbackHost`, `FallbackPort`
- `RedirectMaxRetries`, `RedirectRetryDelayMs`
- `FallbackOnRedirectFailure`
- `TransferBatchSize`, `TransferBatchDelayMs`

### 4. Mantenimiento (`/halt`)
- `HaltEnabled`
- `HaltAnnounceDelayMs`
- `HaltEnableWhitelist`
- `OpGroupName`

### 5. Redireccion en handshake
- `RedirectDuringHandshake`
- `RedirectHost`, `RedirectPort`
- `MaintenanceMode`

### 6. Avisos (`/avisos`)
- `AvisosEnabled`
- `AvisosPrefix`, `AvisosFormat`
- `AvisosAllowPlaceholders`
- `AvisosCooldownSeconds`
- `AvisosMaxLength`

### 7. Estado y placeholders
- `PlaceholderRefreshIntervalSeconds`
- `StatusMaxPlayers`
- `StatusOnlineText`, `StatusOfflineText`
- `PingEstimatedText`

### 8. Auto-restart
- `AutoRestartEnabled`
- `AutoRestartAfterSeconds`
- `AutoRestartWarningsSeconds`

### 9. Mensajes
- `MsgPararAnnounce`, `MsgPararCountdown`, `MsgPararSending`, `MsgPararStopping`, `MsgPararCancelled`
- `MsgAutoRestartWarning`, `MsgAutoRestartStarting`
- `MsgHaltAnnounce`, `MsgHaltSending`, `MsgHaltWhitelistEnabled`

## Build y empaquetado

### Requisitos
1. Java 25
2. Maven 3.x

### Compilar
```bash
mvn clean package
```

Salida:
- `target/ZZDisconect-1.0.4.jar`

### Copiar JAR a Desktop\JAR (PowerShell)
```powershell
$dest = Join-Path $env:USERPROFILE "Desktop\JAR"
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Copy-Item .\target\ZZDisconect-1.0.4.jar -Destination $dest -Force
```

## Instalacion en servidor
1. Deten el servidor.
2. Copia `ZZDisconect-1.0.4.jar` a la carpeta `mods/`.
3. Inicia el servidor.
4. Verifica en consola que el plugin cargue sin errores.

## Flujo recomendado de cambios de config (sin reinicio)
1. Edita el archivo de config.
2. Ejecuta `/zzdisconect reload`.
3. Valida con `/lobbyinfo` y pruebas cortas (`/avisos ... --confirm`, `/parar --confirm` en entorno de test).

## Estado del arte
- Version manifest: `1.0.4`
- Config interna actual: `ModVersion=1.0.5`

## Licencia
MIT. Ver `LICENSE`.
