package fap.pigeon.FapJarvis;

import cn.nukkit.plugin.PluginBase;

/**
 * FapJarvis — 贾维斯助手（活动临时插件）
 *
 * 聊天触发"贾维斯 + 绷住"关键词时给玩家附魔金苹果效果，
 * 冷却时间 = 最长效果持续时间（5分钟），纯内存存储，无数据库。
 */
public class FapJarvisMain extends PluginBase {

    private int cooldownSeconds;
    private String activateMessage;
    private String cooldownMessageTemplate;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        JarvisListener listener = new JarvisListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getCommandMap().register("fapjarvis", new JarvisCommand(this, listener));

        getLogger().info("§a[FapJarvis] 贾维斯助手已启动 §7v" + getDescription().getVersion()
                + " §7| 冷却: " + cooldownSeconds + "s");
    }

    /** 从 config.yml 加载设置（启动 & reload 时调用） */
    void loadSettings() {
        reloadConfig();
        cooldownSeconds = getConfig().getInt("cooldown", 300);
        activateMessage = getConfig().getString("messages.activate",
                "§a已开启绷住模式");
        cooldownMessageTemplate = getConfig().getString("messages.cooldown",
                "§6§l[贾维斯]§r§7 绷住模式冷却中，还需 §e{time}§7。");
    }

    int getCooldownSeconds() {
        return cooldownSeconds;
    }

    String getActivateMessage() {
        return activateMessage;
    }

    /** 传入剩余毫秒，返回格式化的冷却提示消息 */
    String getCooldownMessage(long remainingMillis) {
        long totalSeconds = remainingMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String timeStr = minutes > 0 ? minutes + "分" + seconds + "秒" : seconds + "秒";
        return cooldownMessageTemplate.replace("{time}", timeStr);
    }
}
