
<a name="the-cpmclient-command"/>

# `/cpmclient` 命令

添加于 0.6.15

> [!NOTE]
> Forge 1.14 到 1.17 版本，以及 Fabric 1.19 以下版本不支持客户端命令，  
> 该命令在这些版本上被禁用。  

> [!NOTE]
> 这是客户端 CPM 命令，服务器端命令请查看 `/cpm`，[点击这里](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command-zh-CN)  


<a name="subcommands"/>

### 子命令
* [profile](#profile)
* [safety](#safety)
* [animate](#animate)
* [set_model](#set_model)
* [reset_model](#reset_model)


<a name="profile"/>

## profile
用法：`/cpmclient profile <目标>`  
打开指定玩家的社交设置菜单。  
目标：玩家名

<a name="safety"/>

## safety
打开安全设置菜单。

<a name="animate"/>

## animate
用法：`/cpmclient animate <动画名称> [值（0-255）]`  
为自己播放动画。

仅当动画名称带有 `client:` 前缀时，允许命令控制该动画。

值说明：  
0：重置姿势/动作  
1：播放姿势/动作  
图层值：0-255  
切换值：0-1 或留空以切换状态。

<a name="set_model"/>

### set_model
用法：`/cpmclient set_model <模型文件>`  
将当前模型更改为输入的模型文件。  
仅适用于已安装 Mod 的服务器。  
此命令等同于模型菜单中的对应功能。

<a name="reset_model"/>

### reset_model
删除当前选择的模型，并允许从皮肤加载。  
此命令等同于模型菜单中的“从皮肤加载”按钮。

