# ZZDisconect 1.0.4

ZZDisconect es un plugin para servidores de Hytale desarrollado por ZonaZero, diseñado para gestionar desconexiones, mantenimientos y reinicios automáticos de manera segura y controlada. Permite transferir jugadores a servidores de respaldo (lobby) antes de apagar o reiniciar el servidor, evitando pérdidas de progreso o desconexiones abruptas.

## Funcionalidades Principales

### Comando `/parar --confirm`
- **Descripción**: Inicia una secuencia de apagado completo del servidor.
- **Flujo**:
  1. Anuncia el apagado a todos los jugadores.
  2. Espera un tiempo configurable (`PararAnnounceDelayMs`).
  3. Opcionalmente, muestra una cuenta atrás cada `PararCountdownEverySeconds`.
  4. Transfiere a **todos** los jugadores al servidor de respaldo (FallbackHost/FallbackPort).
  5. Espera `PostTransferDelayMs` para asegurar la transferencia.
  6. Ejecuta el comando interno `/stop` para apagar el servidor.
- **Uso**: `/parar --confirm`
- **Permisos**: Requiere permisos de operador.

### Comando `/move <jugador> <host> <port>`
- **Descripción**: Transfiere a un jugador específico a un servidor dado.
- **Uso**: `/move <jugador> <host> <port>`
- **Ejemplo**: `/move Player1 192.168.1.100 5520`
- **Permisos**: Requiere permisos de operador.

### Comando `/lobbyinfo`
- **Descripción**: Muestra información del lobby, incluyendo lista de jugadores conectados y estado del servidor.
- **Uso**: `/lobbyinfo`
- **Nota**: En futuras versiones, se implementarán hologramas y elementos visuales (PNG) para una mejor presentación.

### Reinicio Automático
- **Descripción**: Programa un reinicio automático después de un tiempo determinado.
- **Configuración**:
  - `AutoRestartEnabled`: Habilita/desabilita el reinicio automático.
  - `AutoRestartAfterSeconds`: Segundos después del inicio para iniciar el reinicio (ej. 7200 = 2 horas).
  - `AutoRestartWarningsSeconds`: Lista de segundos antes del reinicio para enviar avisos (ej. "600,300,60,30,10").
- **Flujo**: Envía avisos en los tiempos configurados y luego ejecuta la secuencia de `/parar`.

### Redirección en Handshake
- **Descripción**: Redirige jugadores durante la conexión si el servidor está en modo mantenimiento.
- **Configuración**:
  - `RedirectDuringHandshake`: Habilita la redirección.
  - `RedirectHost` y `RedirectPort`: Servidor de destino.
  - `MaintenanceMode`: Activa el modo mantenimiento.

## Configuración

Los archivos de configuración se crean automáticamente en la carpeta de datos del plugin:

### `ZZDisconect.json`
```json
{
  "FallbackHost": "127.0.0.1",
  "FallbackPort": 5520,
  "PararEnabled": true,
  "PararAnnounceDelayMs": 5000,
  "PararCountdownEverySeconds": 0,
  "PostTransferDelayMs": 2000,
  "HaltEnabled": true,
  "HaltAnnounceDelayMs": 3000,
  "HaltEnableWhitelist": true,
  "OpGroupName": "op",
  "RedirectDuringHandshake": false,
  "RedirectHost": "127.0.0.1",
  "RedirectPort": 5520,
  "MaintenanceMode": false,
  "AutoRestartEnabled": false,
  "AutoRestartAfterSeconds": 0,
  "AutoRestartWarningsSeconds": "600,300,60,30,10"
}
```

### `messages.json`
```json
{
  "Prefix": "[ZZDisconect] ",
  "PararAnnounce": "El servidor se apagará pronto. En {delay}s serás enviado al lobby.",
  "PararCountdown": "Apagando en {seconds}s...",
  "PararSending": "Enviando a todos al lobby...",
  "PararStopping": "Apagando servidor...",
  "AutoRestartWarning": "Reinicio automático en {seconds}s.",
  "AutoRestartStarting": "Reinicio automático ahora.",
  "HaltAnnounce": "Mantenimiento: se enviará a los jugadores al lobby y se activará la whitelist.",
  "HaltSending": "Enviando al lobby a jugadores (no-op)...",
  "HaltWhitelistEnabled": "Whitelist activada."
}
```

## Instalación y Compilación

### Compilación
```bash
mvn -D"hytale.api.jar=C:\Ruta\A\HytaleServer.jar" clean package
```

### Instalación
1. Copia `target/ZZDisconect-1.0.4.jar` a la carpeta `mods/` del servidor.
2. Reinicia el servidor.

## Consejos de Configuración
- Si las transferencias fallan, aumenta `PostTransferDelayMs` a 3000-5000 ms.
- Para que `/halt` detecte operadores correctamente, asegúrate de que `OpGroupName` coincida con el grupo de operadores (por defecto "op").
- Prueba las configuraciones en un entorno de desarrollo antes de producción.

## Próximas Actualizaciones

### Versión 1.1.0 (Planeada)
- **Sistema de Movimiento entre Servidores**: ✅ Implementado parcialmente con `/move`. Se expandirá con configuraciones predefinidas de servidores.
- **Sistema de Lobby con Hologramas**: ✅ Comando `/lobbyinfo` implementado. En futuras versiones:
  - Integración de hologramas reales para mostrar jugadores.
  - Estado del servidor con indicadores visuales.
  - Soporte para imágenes PNG personalizables.

### Otras Mejoras Futuras
- Soporte para múltiples idiomas en mensajes.
- Integración con APIs externas para monitoreo de servidores.
- Mejoras en la UI para configuraciones in-game.
- Logs más detallados para debugging.

## Soporte
Para soporte o reportes de bugs, contacta al equipo de ZonaZero.

## Licencia
Este proyecto está bajo la licencia MIT. Ver `LICENSE` para más detalles.
