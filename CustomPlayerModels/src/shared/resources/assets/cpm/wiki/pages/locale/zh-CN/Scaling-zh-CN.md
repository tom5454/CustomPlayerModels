
<a name="scaling"/>

## 缩放

- [属性缩放](#attribute-scaling)
- [支持的模组](#supported-mods)


<a name="attribute-scaling"/>

### 属性缩放
> [!NOTE]
> 本章可能已过时。
> 查看本页的英文版本：[跳转](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling#attribute-scaling)

CPM 可以使用[属性](https://zh.minecraft.wiki/w/%E5%B1%9E%E6%80%A7/)来应用部分缩放设置。以下是各个 Minecraft 版本中支持的属性列表：

|     比例选项     | 1.6 - 1.8 | 1.10 | 1.12 | 1.14 - 1.17 | 1.18 - 1.20.4 | 1.20.6 - 1.21.5 | 1.21.6 |
| :--------------: | :-------: | :--: | :--: | :---------: | :-----------: | :-------------: | :----: |
|     实体比例     |    否     |  否  |  否  |     否      |      否       |       是        |   是   |
| 第三人称视角距离 |    否     |  否  |  否  |     否      |      否       |       否        |   是   |
|     动作比例     |    是     |  是  |  是  |     是      |      是       |       是        |   是   |
|   最大行走高度   |    否     |  否  |  否  |     否      |      是       |       是        |   是   |
|   摔落伤害倍数   |    否     |  否  |  否  |     否      |      否       |       是        |   是   |
|     交互距离     |    否     |  否  |  是  |     是      |      是       |       是        |   是   |
|     挖掘速度     |    否     |  否  |  否  |     否      |      否       |       是        |   是   |
|     攻击速度     |    否     |  是  |  是  |     是      |      是       |       是        |   是   |
|       击退       |    否     |  否  |  否  |     是      |      是       |       是        |   是   |
|   攻击伤害比例   |    是     |  是  |  是  |     是      |      是       |       是        |   是   |
|     防御比例     |    否     |  是  |  是  |     是      |      是       |       是        |   是   |
|      生命值      |    是     |  是  |  是  |     是      |      是       |       是        |   是   |
|    生物可见性    |    是     |  是  |  是  |     是      |      是       |       是        |   是   |
|     跳跃高度     |    否     |  否  |  否  |     否      |      否       |       是        |   是   |
|   击退抗性比例   |    是     |  是  |  是  |     是      |      是       |       是        |   是   |
| 安全摔落高度比例 |    否     |  否  |  否  |     否      |      否       |       是        |   是   |
|     缩放选项     | 1.6 - 1.8 | 1.10 | 1.12 | 1.14 - 1.17 | 1.18 - 1.20.4 | 1.20.6 - 1.21.5 | 1.21.6 |

方法名称：`attribute`


<a name="supported-mods"/>

### 支持的模组


<a name="pehkui"/>

#### Pehkui
仅适用于 1.14 及以上版本  
玩家大小、生命值、移动速度及多种属性均可缩放：  
方法名称：`pehkui`  
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/pehkui) | [Modrinth](https://modrinth.com/mod/pehkui)  


<a name="chiseled-me"/>

#### 超级变变变(Chiseled Me)
仅适用于 1.10.2 和 1.12.2  
仅支持实体缩放  
方法名称：`chiseledme`  
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/chiseled-me)  


<a name="artemislib"/>

#### ArtemisLib
仅适用于 1.12.2  
仅支持实体缩放、宽度和高度  
方法名称：`artemislib`  
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/artemislib)  


<a name="server-settings"/>

## 服务器设置
部分属性的缩放默认处于禁用状态，需在服务器端通过 [/cpm 命令](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command-zh-CN#scaling) 启用。
