# YReconnect — Fabric Mod for Minecraft 1.21.1

Auto-disconnects from a server and **instantly reconnects** when your Y coordinate crosses a configurable threshold. Includes a full in-game config GUI.

---

## Features
- ✅ **Enable/Disable** toggle — turn the mod on/off without removing it
- ✅ **Trigger Y** — set any Y value as the threshold
- ✅ **Direction** — trigger when going *below* OR *above* the threshold
- ✅ **Reconnect delay** — 0–200 tick slider (0–10 seconds)
- ✅ **Quick presets** — Void / Y<0 / Y>256 / Y>320 one-click buttons
- ✅ **In-game GUI** — press `K` to open, or run `/yreconnect config`
- ✅ **Persistent config** — settings saved to `.minecraft/config/yreconnect.json`
- ✅ **Client-only** — works on any server, no server-side install needed

---

## Setup & Build

### Requirements
- Java 21+
- Fabric Loader 0.16.5+
- Fabric API 0.102.0+1.21.1
- Minecraft 1.21.1

### Build steps
```bash
./gradlew build            # Mac/Linux
gradlew.bat build          # Windows
```
Output: `build/libs/yreconnect-1.1.0.jar`

Drop the jar into `.minecraft/mods/` alongside **Fabric API**.

---

## Usage

### Open the config screen
- Press **K** while in-game (configurable in Options → Controls)
- Or type `/yreconnect config`

### Config screen controls
| Control | Description |
|---|---|
| Status toggle | Enable or disable the mod |
| Trigger direction | Below or Above the threshold Y |
| Trigger Y field | Type any number (including negative like -64) |
| Delay slider | 0–200 ticks before reconnecting |
| Preset buttons | Void / Y<0 / Y>256 / Y>320 quick-apply |
| Save & Close | Applies and saves all changes |
| Cancel | Discards any unsaved changes |

### Commands
```
/yreconnect config          → Open the GUI
/yreconnect status          → Print current settings in chat
/yreconnect set <y>         → Set trigger Y (default: disconnect below)
/yreconnect set <y> true    → Disconnect when Y ABOVE the value
/yreconnect delay <ticks>   → Set reconnect delay (0–200)
/yreconnect toggle          → Enable / disable
/yreconnect help            → List all commands
```

---

## Config file
`%appdata%\.minecraft\config\yreconnect.json`
```json
{
  "enabled": true,
  "triggerY": 0.0,
  "triggerAbove": false,
  "reconnectDelayTicks": 20
}
```
