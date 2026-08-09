# FapJarvis — 贾维斯助手

> Nukkit-MOT 活动插件 — 聊天触发"贾维斯 + 绷住"关键词，给予附魔金苹果效果 + 播放音效

## 简介

FAPIXEL 小游戏服务器活动临时插件。玩家在聊天中同时发送「贾维斯」和「绷住」即可触发「绷住模式」，获得附魔金苹果全套效果，同时播放专属音效。

## 功能

- 🎮 **聊天触发** — 消息同时包含"贾维斯"和"绷住"即激活
- 🍎 **附魔金苹果效果** — 生命恢复 II / 伤害吸收 IV / 抗性 I / 防火 I
- 🔊 **音效联动** — 激活时通过 FapSound 插件播放 `jarvis` 音效（需安装 FapSound）
- ⏱️ **冷却机制** — 冷却时间 = 最长效果持续时间（默认 5 分钟），纯内存存储

## 效果详情

| 效果 | 等级 | 持续时间 |
|------|------|----------|
| 生命恢复 | II | 30 秒 |
| 伤害吸收 | IV | 2 分钟 |
| 抗性提升 | I | 5 分钟 |
| 防火 | I | 5 分钟 |

## 命令

| 命令 | 说明 |
|------|------|
| `/jarvis reload` | 重载配置 |
| `/jarvis cooldown` | 查看冷却设置 |
| `/jarvis clear` | 清除所有玩家冷却 |

## 配置

```yaml
# plugins/FapJarvis/config.yml
cooldown: 300              # 冷却时间（秒）

messages:
  activate: "§a已开启绷住模式"
  cooldown: "§6§l[贾维斯]§r§7 绷住模式冷却中，还需 §e{time}§7。"
```

## 依赖

- 服务端：[Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT)
- **FapSound**（可选）：激活时播放音效，未安装不影响效果触发

## 构建

```bash
python build.py
```

## License

内部项目，仅供 FAPIXEL 小游戏服务器使用。
