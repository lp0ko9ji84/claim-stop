package com.claimstop;

import com.claimstop.config.ClaimStopConfig;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClaimStop —— 独立的客户端 Mod。
 * 监听聊天消息：当收到“进入他人领地 / 没有許可建造”等指定提示句时，
 * 自动停止当前动作（松开攻击/使用/潜行键），并按需发送自定义指令。
 * 所有聊天反馈文案均通过翻译键（assets/claim-stop/lang/*.json）本地化。
 */
public class ClaimStopClient implements ClientModInitializer {

	private ClaimStopConfig config;
	private long lastTriggerAt = 0L;

	@Override
	public void onInitializeClient() {
		config = ClaimStopConfig.load();

		// 始终注册监听，实际是否生效由 config 里的开关决定（这样 reload 后无需重启）。
		ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
		ClientReceiveMessageEvents.CHAT.register(this::onChatMessage);

		// 客户端指令：/claimstop reload / toggle / status / trigger ... / cmd ...
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("claimstop")
						.executes(ctx -> { status(); return 1; })
						.then(ClientCommands.literal("reload").executes(ctx -> { reload(); return 1; }))
						.then(ClientCommands.literal("toggle").executes(ctx -> { toggle(); return 1; }))
						.then(ClientCommands.literal("status").executes(ctx -> { status(); return 1; }))
						// 触发句管理
						.then(ClientCommands.literal("trigger")
								.then(ClientCommands.literal("list").executes(ctx -> { triggerList(); return 1; }))
								.then(ClientCommands.literal("add")
										.then(ClientCommands.argument("text", StringArgumentType.greedyString())
												.executes(ctx -> { triggerAdd(StringArgumentType.getString(ctx, "text")); return 1; })))
								.then(ClientCommands.literal("set")
										.then(ClientCommands.argument("index", IntegerArgumentType.integer(1))
												.then(ClientCommands.argument("text", StringArgumentType.greedyString())
														.executes(ctx -> { triggerSet(IntegerArgumentType.getInteger(ctx, "index"),
																StringArgumentType.getString(ctx, "text")); return 1; }))))
								.then(ClientCommands.literal("remove")
										.then(ClientCommands.argument("index", IntegerArgumentType.integer(1))
												.executes(ctx -> { triggerRemove(IntegerArgumentType.getInteger(ctx, "index")); return 1; })))
								.then(ClientCommands.literal("clear").executes(ctx -> { triggerClear(); return 1; })))
						// 自动指令管理
						.then(ClientCommands.literal("cmd")
								.then(ClientCommands.literal("list").executes(ctx -> { cmdList(); return 1; }))
								.then(ClientCommands.literal("add")
										.then(ClientCommands.argument("text", StringArgumentType.greedyString())
												.executes(ctx -> { cmdAdd(StringArgumentType.getString(ctx, "text")); return 1; })))
								.then(ClientCommands.literal("remove")
										.then(ClientCommands.argument("index", IntegerArgumentType.integer(1))
												.executes(ctx -> { cmdRemove(IntegerArgumentType.getInteger(ctx, "index")); return 1; })))
								.then(ClientCommands.literal("clear").executes(ctx -> { cmdClear(); return 1; })))
				));
	}

	/** 服务端游戏/系统消息（领地插件提示通常走这里）。 */
	private void onGameMessage(Component message, boolean overlay) {
		if (config.listenGameMessages) {
			handle(message.getString());
		}
	}

	/** 玩家聊天消息。 */
	private void onChatMessage(Component message, PlayerChatMessage playerChatMessage,
			GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp) {
		if (config.listenPlayerChat) {
			handle(message.getString());
		}
	}

	private void handle(String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return; // 未进入游戏时不做任何事
		}
		if (!config.enabled) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastTriggerAt < config.cooldownMs) {
			return; // 冷却中
		}
		if (matches(text)) {
			lastTriggerAt = now;
			if (config.debug) {
				feedbackDebug("claimstop.debug.triggered", text);
			}
			doStop();
		}
	}

	/** 判断消息是否命中任一触发句。 */
	private boolean matches(String text) {
		List<String> patterns = config.triggerPatterns;
		if (patterns == null) {
			return false;
		}
		for (String raw : patterns) {
			if (raw == null || raw.isEmpty()) {
				continue;
			}
			Pattern p = compile(raw);
			Matcher m = p.matcher(text);
			if (config.wholeMessage ? m.matches() : m.find()) {
				return true;
			}
		}
		return false;
	}

	/** 把触发句编译成正则；默认模式下 {player} / {any} 会被展开。 */
	private Pattern compile(String raw) {
		int flags = config.ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
		if (config.useRegex) {
			return Pattern.compile(raw, flags);
		}
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < raw.length()) {
			if (raw.startsWith("{player}", i)) {
				sb.append("(\\S+)"); // 玩家ID：不含空白的连续字符
				i += 8;
			} else if (raw.startsWith("{any}", i)) {
				sb.append("(.*?)");  // 任意内容
				i += 5;
			} else {
				char c = raw.charAt(i++);
				if ("\\.^$|?*+()[]{}".indexOf(c) >= 0) {
					sb.append('\\'); // 转义正则元字符
				}
				sb.append(c);
			}
		}
		return Pattern.compile(sb.toString(), flags);
	}

	/** 触发后的处理：停止动作 + 发送自定义指令。 */
	private void doStop() {
		Minecraft mc = Minecraft.getInstance();

		// 1) 停止当前动作：松开攻击/使用键，从而停止挖方块/放方块。
		if (config.stopActions) {
			try {
				if (mc.options != null) {
					mc.options.keyAttack.setDown(false);
					mc.options.keyUse.setDown(false);
				}
			} catch (Exception e) {
				if (config.debug) {
					feedbackError("claimstop.stop.failed", e.getMessage());
				}
			}
		}

		// 2) 发送自定义指令（无需带“/”）。
		List<String> commands = config.commands;
		if (commands != null && mc.player != null && mc.player.connection != null) {
			for (String c : commands) {
				if (c == null || c.isBlank()) {
					continue;
				}
				String cmd = c.startsWith("/") ? c.substring(1) : c;
				try {
					mc.player.connection.sendCommand(cmd);
				} catch (Exception ignored) {
					// 单个指令失败不影响后续指令
				}
			}
		}
	}

	// ---- 客户端指令处理 ----

	private void reload() {
		config = ClaimStopConfig.load();
		int n = config.triggerPatterns == null ? 0 : config.triggerPatterns.size();
		feedback("claimstop.reload", n);
	}

	private void toggle() {
		config.enabled = !config.enabled;
		feedback(config.enabled ? "claimstop.toggle.on" : "claimstop.toggle.off");
	}

	private void status() {
		feedback("claimstop.status.header");
		feedbackDetail("claimstop.status.flags",
				config.enabled, config.wholeMessage, config.ignoreCase, config.useRegex);
		feedbackDetail("claimstop.status.listen",
				config.listenGameMessages, config.listenPlayerChat, config.stopActions, config.cooldownMs, config.debug);
		if (config.triggerPatterns != null && !config.triggerPatterns.isEmpty()) {
			feedbackDetail("claimstop.status.triggers", config.triggerPatterns.size());
			for (String p : config.triggerPatterns) {
				feedbackDetail("claimstop.trigger.list.line", p);
			}
		} else {
			feedbackDetail("claimstop.status.triggers_none");
		}
		if (config.commands != null && !config.commands.isEmpty()) {
			feedbackDetail("claimstop.status.cmds", config.commands.size(), String.join(", ", config.commands));
		} else {
			feedbackDetail("claimstop.status.cmds_none");
		}
	}

	// ---- 触发句管理 ----

	private void ensureTriggers() {
		if (config.triggerPatterns == null) {
			config.triggerPatterns = new ArrayList<>();
		}
	}

	private void triggerList() {
		ensureTriggers();
		if (config.triggerPatterns.isEmpty()) {
			feedback("claimstop.trigger.none");
			return;
		}
		feedback("claimstop.trigger.list.header", config.triggerPatterns.size());
		for (int i = 0; i < config.triggerPatterns.size(); i++) {
			feedbackDetail("claimstop.trigger.list.line", i + 1, config.triggerPatterns.get(i));
		}
	}

	private void triggerAdd(String text) {
		ensureTriggers();
		config.triggerPatterns.add(text);
		config.save();
		feedback("claimstop.trigger.added", config.triggerPatterns.size(), text);
	}

	private void triggerSet(int index, String text) {
		ensureTriggers();
		if (index < 1 || index > config.triggerPatterns.size()) {
			feedbackError("claimstop.index.oob", config.triggerPatterns.size());
			return;
		}
		config.triggerPatterns.set(index - 1, text);
		config.save();
		feedback("claimstop.trigger.set", index, text);
	}

	private void triggerRemove(int index) {
		ensureTriggers();
		if (index < 1 || index > config.triggerPatterns.size()) {
			feedbackError("claimstop.index.oob", config.triggerPatterns.size());
			return;
		}
		String removed = config.triggerPatterns.remove(index - 1);
		config.save();
		feedback("claimstop.trigger.removed", index, removed);
	}

	private void triggerClear() {
		ensureTriggers();
		int n = config.triggerPatterns.size();
		config.triggerPatterns.clear();
		config.save();
		feedback("claimstop.trigger.cleared", n);
	}

	// ---- 自动指令管理 ----

	private void ensureCommands() {
		if (config.commands == null) {
			config.commands = new ArrayList<>();
		}
	}

	private void cmdList() {
		ensureCommands();
		if (config.commands.isEmpty()) {
			feedback("claimstop.cmd.none");
			return;
		}
		feedback("claimstop.cmd.list.header", config.commands.size());
		for (int i = 0; i < config.commands.size(); i++) {
			feedbackDetail("claimstop.cmd.list.line", i + 1, config.commands.get(i));
		}
	}

	private void cmdAdd(String cmd) {
		ensureCommands();
		String c = cmd.startsWith("/") ? cmd.substring(1) : cmd;
		config.commands.add(c);
		config.save();
		feedback("claimstop.cmd.added", config.commands.size(), c);
	}

	private void cmdRemove(int index) {
		ensureCommands();
		if (index < 1 || index > config.commands.size()) {
			feedbackError("claimstop.index.oob", config.commands.size());
			return;
		}
		String removed = config.commands.remove(index - 1);
		config.save();
		feedback("claimstop.cmd.removed", index, removed);
	}

	private void cmdClear() {
		ensureCommands();
		int n = config.commands.size();
		config.commands.clear();
		config.save();
		feedback("claimstop.cmd.cleared", n);
	}

	// ---- 聊天反馈（可本地化） ----

	/** 普通提示。 */
	private void feedback(String key, Object... args) {
		send(Component.literal("§a[ClaimStop] §f").append(Component.translatable(key, args)));
	}

	/** 错误提示。 */
	private void feedbackError(String key, Object... args) {
		send(Component.literal("§c[ClaimStop] §f").append(Component.translatable(key, args)));
	}

	/** 明细行（灰色、缩进）。 */
	private void feedbackDetail(String key, Object... args) {
		send(Component.literal("§7  ").append(Component.translatable(key, args)));
	}

	/** 调试提示。 */
	private void feedbackDebug(String key, Object... args) {
		send(Component.literal("§7[ClaimStop] §f").append(Component.translatable(key, args)));
	}

	/** 向玩家聊天栏发送组件（客户端本地显示）。 */
	private void send(Component msg) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			try {
				mc.player.sendSystemMessage(msg);
			} catch (Exception ignored) {
				// 忽略反馈失败
			}
		}
	}
}
