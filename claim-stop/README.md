# ClaimStop —— 领地自动停止 Mod（Minecraft 26.2 / Fabric）

一个**独立、纯客户端**的 Fabric Mod：监听聊天消息，当收到“进入他人领地 / 没有許可建造”等指定提示句时，
**自动停止当前动作**（松开正在挖方块 / 放方块所按住的攻击/使用键），并按需**发送自定义指令**。

## 功能

- 监听服务端系统/游戏消息（领地插件的提示通常属于这类），也支持监听玩家聊天消息。
- 触发句完全可自定义，支持占位符：
  - `{player}` —— 匹配任意玩家ID（不含空白的连续字符）
  - `{any}`    —— 匹配任意内容（含空格）
- 默认触发句即你要求的领地提示：
  `[領地]您沒有 {player} 的許可在這裡建造。`
- 命中后默认“停止”动作；可配置改为只发送自定义指令，或两者同时执行。
- 内置客户端指令 `/claimstop`，无需重启即可重载配置、开关、查看状态、增删改触发句与自动指令。
- 聊天提示多语言：英文 / 简体中文 / 繁体中文。

## 环境要求

- Minecraft **26.2**
- Fabric Loader **>= 0.19.3**
- Fabric API（本机已装 `fabric-api 0.161.0+26.2`）
- Java **25 及以上**（构建 JDK 需 ≤ Java 26，见下方“重建”）

## 安装（使用成品 jar）

1. 把 `build/libs/claim-stop-1.0.0.jar` 放进 `.minecraft/mods/`（若开版本隔离，放入 `versions\<版本>\mods\`）。
2. 确保已安装对应 26.2 的 Fabric Loader 和 Fabric API。
3. 启动游戏进入服务器。首次运行会在 `config/` 自动生成 `claim-stop.json`（即默认配置）。
4. 需要改触发句 / 自定义指令时，可在游戏里用 `/claimstop trigger ...`、`/claimstop cmd ...` 直接修改，改完自动写回配置并立即生效。

## 客户端指令

| 指令 | 作用 |
|---|---|
| `/claimstop` | 查看当前配置 |
| `/claimstop status` | 显示全部参数 |
| `/claimstop reload` | 重新读取 `config/claim-stop.json` |
| `/claimstop toggle` | 快速开关本 Mod |
| `/claimstop trigger list/add/set/remove/clear` | 列出/新增/修改/删除/清空触发句 |
| `/claimstop cmd list/add/remove/clear` | 列出/新增/删除/清空自动指令 |

## 配置说明（config/claim-stop.json）

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

| 字段 | 默认 | 说明 |
|---|---|---|
| `enabled` | true | 总开关 |
| `wholeMessage` | false | true=整条消息完全匹配；false=消息包含关键句即触发 |
| `ignoreCase` | false | 是否忽略大小写 |
| `useRegex` | false | true 时把 `triggerPatterns` 当作正则表达式（此时 `{player}/{any}` 不再生效） |
| `listenGameMessages` | true | 监听服务端系统/游戏消息（领地提示一般走这里） |
| `listenPlayerChat` | false | 是否也监听玩家聊天消息 |
| `triggerPatterns` | 领地提示 | 触发句列表，支持 `{player}` / `{any}` |
| `stopActions` | false | 命中后是否松开攻击/使用键以停止挖/放方块 |
| `commands` | ["rtp"] | 命中后发送的自定义指令（**不要带 `/`**，可多条） |
| `cooldownMs` | 1000 | 两次触发之间的冷却（毫秒） |
| `debug` | false | 命中时在聊天栏打印匹配到的原文 |

## 重建（从源码构建）

项目已包含 Gradle Wrapper。注意：**本机若只有 JDK 27，Gradle 9.5.1 无法在其上运行**
（会报 `Unsupported class file major version 71`），需要先用 JDK 25（或 26）构建。

```powershell
# 设 JAVA_HOME 指向 JDK 25 或 26（示例）
$env:JAVA_HOME = "C:\Users\mlknjjj\jdk-25-toolchain\jdk-25.0.4.1+1"

.\gradlew.bat build
# 产物在 build/libs/claim-stop-1.0.0.jar
```

主要版本（见 `gradle.properties`）：Minecraft `26.2`、Fabric Loader `0.19.5`、Loom `1.17.21`、
Fabric API `0.161.0+26.2`、Java release `25`。

## 文件结构

```
claim-stop/
├── build.gradle / settings.gradle / gradle.properties
├── gradlew(.bat)  gradle/wrapper/…
├── src/main/java/com/claimstop/
│   ├── ClaimStopClient.java          # 主逻辑：监听、匹配、停止、发指令
│   └── config/ClaimStopConfig.java   # 配置加载
└── src/main/resources/
    ├── fabric.mod.json
    └── assets/claim-stop/
        ├── icon.png
        └── lang/{en_us,zh_cn,zh_tw}.json
```

## 隐私与合规

本 Mod 仅在**你自己的客户端**本地运行：监听的是发到你自己屏幕上的聊天消息，松开的是你自己的按键，
发送的是你自己账户执行的服务端指令。它不会修改/读取他人的游戏、不会触碰账号或其他玩家数据。

© F隐秘的㕑师🍴 · MIT License
