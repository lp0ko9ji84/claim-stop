package com.claimstop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ClaimStop 配置文件（位于 config/claim-stop.json）。
 * 所有字段都有默认值，即使配置文件中缺字段也会沿用默认值。
 */
public class ClaimStopConfig {

	/** 总开关。 */
	public boolean enabled = true;

	/** 是否要求消息整句匹配（true 用 matches()；false 只要包含关键字即触发）。 */
	public boolean wholeMessage = false;

	/** 是否忽略大小写。 */
	public boolean ignoreCase = false;

	/** 是否把 triggerPatterns 当作正则表达式处理（true 时 {player}/{any} 不再生效）。 */
	public boolean useRegex = false;

	/** 是否监听服务端系统/游戏消息（领地插件的提示大多属于这类）。 */
	public boolean listenGameMessages = true;

	/** 是否监听玩家聊天消息（消息中含有指定关键句时也会触发）。 */
	public boolean listenPlayerChat = false;

	/**
	 * 触发句列表。
	 * 支持占位符：
	 *   {player} —— 匹配任意玩家ID（不含空白的连续字符）
	 *   {any}    —— 匹配任意内容（含空格）
	 * 默认值即领地插件提示句。
	 */
	public List<String> triggerPatterns =
			new ArrayList<>(List.of("[領地]您沒有 {player} 的許可在這裡建造。"));

	/** 是否执行“停止”动作：松开当前按住方块的攻击/使用键，停止挖方块/放方块。 */
	public boolean stopActions = true;

	/** 触发后要发送的自定义指令（无需带“/”，例如 "stop" 或 "abort"）。可留空。 */
	public List<String> commands = new ArrayList<>();

	/** 两次触发之间的冷却时间（毫秒），防止刷屏重复触发。 */
	public int cooldownMs = 1000;

	/** 调试：触发时在聊天栏打印匹配到的原文。 */
	public boolean debug = false;

	/** 运行时记住配置文件路径（transient：不会序列化进 JSON）。 */
	public transient Path configPath;

	/** 从 config/claim-stop.json 加载配置；文件不存在时自动生成默认配置。 */
	public static ClaimStopConfig load() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		Path file = dir.resolve("claim-stop.json");
		ClaimStopConfig cfg;
		if (Files.exists(file)) {
			try (Reader r = Files.newBufferedReader(file)) {
				cfg = new Gson().fromJson(r, ClaimStopConfig.class);
			} catch (IOException e) {
				cfg = new ClaimStopConfig();
			}
			if (cfg == null) {
				cfg = new ClaimStopConfig();
			}
		} else {
			cfg = new ClaimStopConfig();
			save(cfg, file);
		}
		cfg.configPath = file;
		return cfg;
	}

	/** 把当前配置写回它原本加载的文件（供运行时修改后持久化）。 */
	public void save() {
		if (configPath != null) {
			save(this, configPath);
		}
	}

	/** 把配置写回磁盘（带缩进，便于手动编辑）。 */
	public static void save(ClaimStopConfig cfg, Path file) {
		try (Writer w = Files.newBufferedWriter(file)) {
			new GsonBuilder().setPrettyPrinting().create().toJson(cfg, w);
		} catch (IOException ignored) {
			// 写入失败不影响运行，仅提示用户。
		}
	}
}
