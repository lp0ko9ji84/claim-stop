# ClaimStop —— 领地自动停止 / 自动指令 Mod

> **作者**：F隐秘的㕑师🍴　｜　**环境**：纯客户端（Client）　｜　**加载器**：Fabric
> **适配版本**：Minecraft 26.2　｜　**依赖**：Fabric API　｜　**许可**：MIT

---

## 一、这个 Mod 是干什么的？

在服务器里不小心走进**别人圈的地**、或者在对方领地里想放方块/挖方块时，领地插件会弹出一条提示，例如：

```
[領地]您沒有 (玩家id) 的許可在這裡建造。
```

**ClaimStop** 会在聊天里**监听这条提示**，一旦出现，就自动帮你执行设定好的动作：

- 默认可**自动停止**当前动作（松开正在按住的挖/放键，不再浪费方块）；
- 也可配置为**只发送一条自定义指令**，例如自动 `/rtp` 随机传送离开；
- 触发句、要发的指令都可以自由增删改，并且**在游戏里用聊天指令就能改**，改完立即生效。

一句话：**进了别人的领地，自动帮你停下来 / 直接传送走。**

---

## 二、特性一览

| 特性 | 说明 |
|---|---|
| 🎯 触发句可自定义 | 支持 `{player}`（任意玩家ID）、`{any}`（任意内容）占位符 |
| ⚡ 自动停止动作 | 松开攻击/使用键，停止挖方块/放方块 |
| 🚀 自动发送指令 | 命中后执行自定义指令，如 `/rtp`、`/back`、`/res` 等，可多条 |
| 💬 聊天指令改配置 | `/claimstop trigger ...`、`/claimstop cmd ...` 增删改，无需重启 |
| 🔁 即时写回配置 | 每次改动自动保存到 `config/claim-stop.json` |
| 🌐 多语言 | 内置 英文 / 简体中文 / 繁体中文，随游戏语言切换 |
| ⏱ 防刷屏冷却 | 可设置触发间隔，避免连续弹提示时重复执行 |
| 🛡 纯本地运行 | 仅监听发到你屏幕上的消息、执行你自己账户的指令，不碰其他玩家 |

---

## 三、安装

1. 将 `claim-stop-1.0.0.jar` 放入 `.minecraft/mods/`（若开了版本隔离，放入 `versions\<版本>\mods\`）。
2. 确保同目录有 **Fabric API**（本 Mod 必需）。
3. 启动游戏（Fabric 26.2），首次运行自动生成 `config/claim-stop.json`。

> 需要的环境：Minecraft 26.2 + Fabric Loader ≥ 0.19.3 + Fabric API + Java 25。

---

## 四、指令一览

| 指令 | 作用 |
|---|---|
| `/claimstop` | 查看当前配置 |
| `/claimstop status` | 显示全部参数 |
| `/claimstop reload` | 重新读取配置文件 |
| `/claimstop toggle` | 快速开关本 Mod |
| `/claimstop trigger list` | 列出触发句 |
| `/claimstop trigger add <句子>` | 新增触发句 |
| `/claimstop trigger set <编号> <句子>` | 修改触发句 |
| `/claimstop trigger remove <编号>` | 删除触发句 |
| `/claimstop trigger clear` | 清空触发句 |
| `/claimstop cmd list` | 列出自动指令 |
| `/claimstop cmd add <指令>` | 新增自动指令（自动去掉 `/`）|
| `/claimstop cmd remove <编号>` | 删除自动指令 |
| `/claimstop cmd clear` | 清空自动指令 |

> 所有 `trigger` / `cmd` 改动都会**立即生效并写回配置文件**，无需重启。

---

## 五、配置说明（config/claim-stop.json）

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
| `wholeMessage` | false | true=整句匹配；false=包含关键句即触发 |
| `ignoreCase` | false | 是否忽略大小写 |
| `useRegex` | false | true 时触发句按正则处理（`{player}/{any}` 失效）|
| `listenGameMessages` | true | 监听系统/游戏消息（领地提示走这类）|
| `listenPlayerChat` | false | 是否也监听玩家聊天 |
| `triggerPatterns` | 领地提示句 | 触发句列表，支持 `{player}` / `{any}` |
| `stopActions` | false | 命中后是否自动停止（松开挖/放键）|
| `commands` | ["rtp"] | 命中后自动发送的指令（不带 `/`）|
| `cooldownMs` | 1000 | 两次触发的冷却（毫秒）|
| `debug` | false | 命中时打印匹配原文 |

---

## 六、多语言

本 Mod 的聊天提示文案已本地化，内置：

- 🇬🇧 英文 `en_us`
- 🇨🇳 简体中文 `zh_cn`
- 🇨🇳 繁体中文 `zh_tw`

跟随游戏内语言设置自动切换，也可自行往语言文件里加翻译。

---

## 七、隐私与安全

- 只在**你自己的客户端**本地运行；
- 只读取**发到你屏幕上的聊天消息**；
- 只松开**你自己的按键**、发送**你自己账户**的服务端指令；
- 不修改他人数据、不读取他人游戏、不上传任何信息。

---

*© F隐秘的㕑师🍴　·　ClaimStop v1.0.0　·　MIT License*
