ZZDisconect 1.0.4 (Maven)

✅ /parar --confirm
 - Broadcast anuncia
 - Espera PararAnnounceDelayMs (opcional: cuenta atrás cada PararCountdownEverySeconds)
 - Manda A TODOS al lobby (FallbackHost/FallbackPort)
 - Espera PostTransferDelayMs
 - Ejecuta /stop (comando del servidor)

✅ /halt --confirm
 - Broadcast anuncia mantenimiento
 - Espera HaltAnnounceDelayMs
 - Manda SOLO NO-OPs al lobby (ops se quedan)
 - Activa whitelist (opcional) ejecutando: whitelist enable
 - NO apaga el servidor

✅ AutoRestart
 - Si AutoRestartEnabled=true y AutoRestartAfterSeconds>0:
   - Envia avisos en los segundos listados en AutoRestartWarningsSeconds (CSV)
   - Al final ejecuta el flujo de /parar automáticamente

Archivos de configuración (se crean en el data folder del plugin):
- ZZDisconect.json  (timers/hosts/flags)
- messages.json     (todos los textos)

Build:
  mvn -D"hytale.api.jar=C:\HytalePL\api\HytaleServer.jar" clean package

Install:
  Copia target\ZZDisconect-1.0.4.jar a mods\ y reinicia.

Config tips:
- Si el referral no llega, sube PostTransferDelayMs a 3000-5000.
- Para que /halt detecte ops, OpGroupName debe coincidir con el grupo de op (por defecto "op").
