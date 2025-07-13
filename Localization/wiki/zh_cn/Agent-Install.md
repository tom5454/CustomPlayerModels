<a name="java-agent-installation"/>

## Java 代理安装

Minecraft 1.2.5 版本的 CPM 是一个 Java 代理。

1. 将 CPM 的 jar 文件复制到你的 `.minecraft` 文件夹中。  
2. 然后添加以下 JVM 参数：  
`-javaagent:CustomPlayerModels-1.2.5-<version>.jar`  
将 `<version>` 替换成你安装的版本号。  

更新 CPM 时，需要同时更新该 JVM 参数。

此版本需要 Minecraft Forge 1.2.5 支持。

此版本的 CPM 内置了类似“单人游戏命令”（Single Player Commands）的 Mod。  
添加 JVM 参数 `-Dcpmcore.spc=true` 可启用该功能。  
支持的命令包括：  
gamemode、tp、time、give、toggledownfall、cpm、cpmclient
