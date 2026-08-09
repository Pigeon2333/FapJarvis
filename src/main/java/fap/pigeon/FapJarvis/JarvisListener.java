package fap.pigeon.FapJarvis;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.network.protocol.MobEffectPacket;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.potion.Effect;
import fap.pigeon.FapSound.FapSoundMain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天监听器 — 检测"贾维斯 + 绷住"关键词，触发附魔金苹果效果
 */
public class JarvisListener implements Listener {

    private final FapJarvisMain plugin;

    /** 冷却结束时间戳（毫秒），key = 玩家 UUID */
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public JarvisListener(FapJarvisMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        String msg = event.getMessage();
        // 模糊匹配：同时包含"贾维斯"和"绷住"即触发
        if (!msg.contains("贾维斯") || !msg.contains("绷住")) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // 冷却检查
        Long expireTime = cooldowns.get(uuid);
        if (expireTime != null && now < expireTime) {
            player.sendMessage(plugin.getCooldownMessage(expireTime - now));
            return;
        }

        // 施加附魔金苹果效果
        applyEnchantedGoldenApple(player);

        // 播放 jarvis 音效（调用 FapSound 插件）
        playJarvisSound(player);

        // 设置冷却（= 最长效果持续时间）
        cooldowns.put(uuid, now + (long) plugin.getCooldownSeconds() * 1000);

        player.sendMessage(plugin.getActivateMessage());
    }

    /**
     * 通过 FapSound 插件播放 jarvis 音效
     */
    private void playJarvisSound(Player player) {
        try {
            Plugin fapSound = player.getServer().getPluginManager().getPlugin("FapSound");
            if (fapSound instanceof FapSoundMain) {
                FapSoundMain soundPlugin = (FapSoundMain) fapSound;
                boolean ok = soundPlugin.playSound(player, "jarvis", 1.0f, 1.0f);
                if (!ok) {
                    plugin.getLogger().info("§7[FapJarvis] FapSound 中未找到音效: jarvis");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().info("§7[FapJarvis] 播放 jarvis 音效失败: " + e.getMessage());
        }
    }

    /**
     * 附魔金苹果（Enchanted Golden Apple）原版效果：
     * - 生命恢复 II (amplifier=1) — 30秒
     * - 伤害吸收 IV (amplifier=3) — 2分钟
     * - 抗性提升 I (amplifier=0) — 5分钟
     * - 防火 I (amplifier=0) — 5分钟
     */
    private void applyEnchantedGoldenApple(Player player) {
        try {
            Server server = player.getServer();
            long tick = player.getLevel().getCurrentTick();

            // --- 效果 1: 生命恢复 II (30秒) ---
            Effect regen = Effect.getEffect(Effect.REGENERATION)
                    .setAmplifier(1).setDuration(20 * 30).setVisible(true);
            player.addEffect(regen);
            plugin.getLogger().info("[FapJarvis] 施加 Regeneration: id=" + regen.getId()
                    + " amp=" + regen.getAmplifier() + " dur=" + regen.getDuration()
                    + " tick=" + tick);

            // --- 效果 2: 伤害吸收 IV (2分钟) ---
            Effect absorption = Effect.getEffect(Effect.ABSORPTION)
                    .setAmplifier(3).setDuration(20 * 120).setVisible(true);
            player.addEffect(absorption);
            plugin.getLogger().info("[FapJarvis] 施加 Absorption: id=" + absorption.getId()
                    + " amp=" + absorption.getAmplifier() + " dur=" + absorption.getDuration());

            // --- 效果 3: 抗性提升 I (5分钟) ---
            Effect resistance = Effect.getEffect(Effect.RESISTANCE)
                    .setAmplifier(0).setDuration(20 * 300).setVisible(true);
            player.addEffect(resistance);
            plugin.getLogger().info("[FapJarvis] 施加 Resistance: id=" + resistance.getId()
                    + " amp=" + resistance.getAmplifier() + " dur=" + resistance.getDuration());

            // --- 效果 4: 防火 I (5分钟) ---
            Effect fireRes = Effect.getEffect(Effect.FIRE_RESISTANCE)
                    .setAmplifier(0).setDuration(20 * 300).setVisible(true);
            player.addEffect(fireRes);
            plugin.getLogger().info("[FapJarvis] 施加 FireResistance: id=" + fireRes.getId()
                    + " amp=" + fireRes.getAmplifier() + " dur=" + fireRes.getDuration());

            // 备选：如果标准 API 不生效，手动发送 MobEffectPacket 补一次
            sendMobEffectPackets(player, tick);

            plugin.getLogger().info("[FapJarvis] 附魔金苹果效果已施加给 " + player.getName()
                    + " | 玩家当前效果数: " + player.getEffects().size());

        } catch (Exception e) {
            plugin.getLogger().error("[FapJarvis] 施加效果时异常: " + e.getMessage(), e);
        }
    }

    /**
     * 手动发送 MobEffectPacket — 作为 addEffect 的补充，
     * 确保客户端收到效果数据包。
     */
    private void sendMobEffectPackets(Player player, long tick) {
        sendEffectPacket(player, Effect.REGENERATION, 1, 20 * 30, tick);
        sendEffectPacket(player, Effect.ABSORPTION, 3, 20 * 120, tick);
        sendEffectPacket(player, Effect.RESISTANCE, 0, 20 * 300, tick);
        sendEffectPacket(player, Effect.FIRE_RESISTANCE, 0, 20 * 300, tick);
    }

    /**
     * 构造并发送单个效果包
     */
    private void sendEffectPacket(Player player, int effectId, int amplifier, int duration, long tick) {
        try {
            MobEffectPacket pk = new MobEffectPacket();
            pk.eid = player.getId();
            pk.effectId = effectId;
            pk.amplifier = amplifier;
            pk.particles = true;
            pk.duration = duration;
            pk.eventId = MobEffectPacket.EVENT_ADD;
            pk.tick = tick;
            player.dataPacket(pk);
        } catch (Exception e) {
            plugin.getLogger().warning("[FapJarvis] 发送效果包异常 effectId=" + effectId + ": " + e.getMessage());
        }
    }

    // ---- 冷却 Map 操作（供 JarvisCommand 调用） ----

    /** 获取玩家剩余冷却毫秒数，0 表示无冷却 */
    public long getRemainingCooldown(UUID uuid) {
        Long expireTime = cooldowns.get(uuid);
        if (expireTime == null) return 0;
        long remaining = expireTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /** 手动清除玩家冷却（管理命令用） */
    public void clearCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
