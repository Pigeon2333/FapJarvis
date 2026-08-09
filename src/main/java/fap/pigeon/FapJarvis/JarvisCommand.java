package fap.pigeon.FapJarvis;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;

/**
 * /jarvis 管理命令
 * - reload        重载配置（OP）
 * - cooldown      查看自己的冷却（所有玩家）
 * - clear <玩家>  清除指定玩家的冷却（OP）
 */
public class JarvisCommand extends Command {

    private final FapJarvisMain plugin;
    private final JarvisListener listener;

    public JarvisCommand(FapJarvisMain plugin, JarvisListener listener) {
        super("jarvis", "贾维斯助手命令");
        this.setPermission("");
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!checkOp(sender)) return false;
                plugin.reloadConfig();
                plugin.loadSettings();
                sender.sendMessage("§a[FapJarvis] 配置已重载");
                return true;
            }
            case "cooldown" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c该命令只能由玩家执行");
                    return false;
                }
                long remaining = listener.getRemainingCooldown(player.getUniqueId());
                if (remaining <= 0) {
                    sender.sendMessage("§a[FapJarvis] 当前无冷却，随时可以触发");
                } else {
                    sender.sendMessage(plugin.getCooldownMessage(remaining));
                }
                return true;
            }
            case "clear" -> {
                if (!checkOp(sender)) return false;
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /jarvis clear <玩家名>");
                    return false;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§c玩家不在线或不存在: " + args[1]);
                    return false;
                }
                listener.clearCooldown(target.getUniqueId());
                sender.sendMessage("§a[FapJarvis] 已清除 " + target.getName() + " 的冷却");
                return true;
            }
            default -> {
                sendUsage(sender);
                return false;
            }
        }
    }

    private boolean checkOp(CommandSender sender) {
        if (sender.isOp() || sender instanceof ConsoleCommandSender) return true;
        sender.sendMessage("§c你没有权限执行此命令");
        return false;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6=== 贾维斯助手 ===");
        sender.sendMessage("§7/jarvis reload §f— 重载配置");
        sender.sendMessage("§7/jarvis cooldown §f— 查看冷却状态");
        if (sender.isOp() || sender instanceof ConsoleCommandSender) {
            sender.sendMessage("§7/jarvis clear <玩家> §f— 清除冷却");
        }
    }
}
