
# ClaimStop — Automatic Action Hault / Command Execution for Land Claims

> **Author**: F-HiddenChef (F隐秘的㕑师🍴) | **Environment**: Client-side | **Loader**: Fabric
> **Supported Version**: Minecraft 26.2 | **Dependency**: Fabric API | **License**: MIT

---

## 1. What does this mod do?

When playing on a server, if you accidentally wander into **someone else's claimed land**—or attempt to place or break blocks within it—the land-protection plugin will typically display a notification, such as:

```
[Claim] You do not have permission to build here (in [PlayerID]'s claim).
```

**ClaimStop** monitors your chat for these specific notifications. Once detected, it automatically triggers a pre-configured action:

- By default, it can **halt your current action** (releasing the mouse button you are holding to stop mining or placing blocks, preventing waste).
- Alternatively, it can be configured to **execute a custom command**—for example, automatically running `/rtp` to teleport to a random location and leave the area.
- You can freely add, remove, or modify both the trigger phrases and the commands to be executed—all via in-game chat commands, with changes taking effect immediately.

In short: **If you enter someone else's claimed land, it automatically stops your current action or teleports you away.** **

---

## II. Feature Overview

| Feature | Description |
|---|---|
| 🎯 Customizable Trigger Phrases | Supports placeholders: `{player}` (any player ID) and `{any}` (any content). |
| ⚡ Automatic Action Stoppage | Stops block mining or placement when the attack/use key is released. |
| 🚀 Automated Command Execution | Executes custom commands (e.g., `/rtp`, `/back`, `/res`) upon triggering; supports multiple commands. |
| 💬 In-Game Configuration | Modify triggers and commands via commands like `/claimstop trigger ...` and `/claimstop cmd ...`—no restart required. |
| 🔁 Real-time Configuration Saving | Changes are automatically saved to `config/claim-stop.json`. |
| 🌐 Multilingual Support | Includes English, Simplified Chinese, and Traditional Chinese; automatically adapts to your game's language setting. |
| ⏱ Anti-Spam Cooldown | Configurable trigger interval to prevent redundant command execution during rapid-fire notifications. |
| 🛡 Local-Only Operation | Operates exclusively on your client—monitors messages displayed on your screen and executes commands from your own account; does not interact with other players. |

---

## III. Installation

1. Place `claim-stop-1.0.0.jar` into your `.minecraft/mods/` folder.
2. Ensure **Fabric API** is present in the same folder (required for this mod).
3. Launch the game (Minecraft 26.2); the `config/claim-stop.json` file will be generated automatically upon the first run.

> Requirements: Minecraft 26.2 + Fabric Loader ≥ 0.19.3 + Fabric API + Java 25.

---

## IV. Command List

| Command | Function |
|---|---|
| `/claimstop` | View current configuration |
| `/claimstop status` | Display all parameters |
| `/claimstop reload` | Reload the configuration file |
| `/claimstop toggle` | Quickly enable/disable the mod |
| `/claimstop trigger list` | List all configured triggers | | List trigger phrases |
| `/claimstop trigger add <phrase>` | Add a trigger phrase |
| `/claimstop trigger set <index> <phrase>` | Modify a trigger phrase |
| `/claimstop trigger remove <index>` | Remove a trigger phrase |
| `/claimstop trigger clear` | Clear all trigger phrases |
| `/claimstop cmd list` | List automatic commands |
| `/claimstop cmd add <command>` | Add an automatic command (the leading `/` is automatically removed) |
| `/claimstop cmd remove <index>` | Remove an automatic command |
| `/claimstop cmd clear` | Clear all automatic commands |

> All changes to `trigger` or `cmd` take effect **immediately and are saved to the configuration file**; no restart is required.

---

## V. Configuration (config/claim-stop.json)

```json
{
"enabled": true,
"wholeMessage": false,
"ignoreCase": false,
"useRegex": false,
"listenGameMessages": true,
"listenPlayerChat": false,
"triggerPatterns": [
"[領地]您沒有 {player} 的許可在這裡建造。"
],
"stopActions": false,
"commands": [
"rtp"
],
"cooldownMs": 1000,
"debug": false
}
```

| Field | Default | Description |
|---|---|---|
| `enabled` | true | Master switch |
| `wholeMessage` | false | `true` = match the entire message; `false` = trigger if the phrase is contained within the message |
| `ignoreCase` | false | Whether to ignore case |
| `useRegex` | false | If `true`, trigger phrases are treated as regular expressions (`{player}`/`{any}` placeholders will not work) |
| `listenGameMessages` | true | Listen for system/game messages (e.g., land/claim notifications) |
| `listenPlayerChat` | false | Whether to also listen for player chat |
| `triggerPatterns` | Land notification phrases | List of trigger phrases; supports... `{player}` / `{any}` |
| `stopActions` | false | Whether to automatically stop actions (release the mine/place key) upon a match |
| `commands` | ["rtp"] | Commands to be sent automatically upon a match (without the `/` prefix) |
| `cooldownMs` | 1000 | Cooldown period between triggers (in milliseconds) |
| `debug` | false | Whether to print the matched text to the console upon a match |

---

## VI. Multilingual Support

Chat notification messages in this mod are localized and include support for:

- 🇬🇧 English (`en_us`)
- 🇨🇳 Simplified Chinese (`zh_cn`)
- 🇨🇳 Traditional Chinese (`zh_tw`)

The language switches automatically based on your in-game settings; you may also add your own translations to the language files.

---

## VII. Privacy and Security

- Runs locally **only on your own client**;
- Reads **only the chat messages displayed on your screen**;
- Releases **only your own keys** and sends server commands **only from your own account**;
- Does not modify data belonging to others, access others' gameplay, or upload any information.

---

*© F-Hidden-Chef🍴 · ClaimStop v1.0.0 · MIT License*
