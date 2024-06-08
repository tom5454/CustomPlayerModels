# `/cpmclient` 命令

添加自 0.6.15

> [!NOTE]
> Forge 1.14 到 1.17 之间的版本和 Fabric 1.19 以下的版本不支持客户端命令
> 此命令在这些版本上被禁用。

> [!NOTE]
> 这是客户端 CPM 命令，请查看服务器端 `/cpm` 命令，[在这里](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command)

### 子命令
* [profile](#profile)
* [safety](#safety)
* [animate](#animate)
* [set_model](#set_model)
* [reset_model](#reset_model)

## profile
用法：`/cpmclient profile <目标>`

打开指定玩家的社交设置菜单
目标：玩家名

## safety
打开安全设置菜单

## animate
用法：`/cpmclient animate <动画名称> [值（0-255）]`
为自己播放动画
仅当具有“client:”前缀时才允许命令控制的动画。
值：0：重置姿势/手势，1：播放姿势/手势，对于图层值：0-255，切换：0-1 或留空以切换状态。

### set_model
用法：`/cpmclient set_model <模型文件>`
将当前模型更改为输入的模型文件。
仅适用于安装了 mod 的服务器。
这与模型菜单里的功能相同。

### reset_model
删除当前选择的模型，并从皮肤加载。
这与使用“模型菜单/从皮肤加载”按钮相同。