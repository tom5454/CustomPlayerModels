## Java Agent 安装

Minecraft 1.2.5版本的CPM是一个Java Agent。

1. 将 CPM jar 复制到 `.minecraft` 文件夹中。
2. 然后添加以下 JVM 参数：
`-javaagent:CustomPlayerModels-1.2.5-<version>.jar`
将 `<version>` 替换为您安装的版本。

更新 CPM 时必须更新 JVM 参数。

需要 Minecraft Forge 1.2.5

这个版本的 CPM 附带了一个类似于 mod 的“单人游戏命令”。
添加 `-Dcpmcore.spc=true` JVM 参数以启用。
支持的命令：
gamemode, tp, time, give, toggledownfall, cpm, cpmclient
