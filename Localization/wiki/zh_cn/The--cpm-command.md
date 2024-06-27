# `/cpm` 命令

> [!NOTE]
> 这是服务器端 CPM 命令，请查看客户端“/cpm client”命令，[在这里](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpmclient-command)

### 子命令：
* [setskin](#setskin)
* [safety](#safety)
* [kick](#kick)
* [scaling](#scaling)
* [animate](#animate)
* [effects](#effects)
* [detect](#detect)

## setskin
用法：`/cpm setskin [-f|-t|-r] <目标> <base64 模型>`
参数：
* -f：强制设定模型，玩家无法更改自己的模型。
* -t：临时模型，设定的模型会在玩家退出游戏时被清除。
* -r：重置模型，移除服务器设置模型

Target：要设置模型的玩家
Base64模型：使用Base64选项导出的模型。
请查看：[Base64 导出](https://github.com/tom5454/CustomPlayerModels/wiki/Exporting#base64)

## safety
子命令：
* [recommend](#safety-recommend)
* [set](#safety-set)

### safety recommend
用法：`/cpm safety recommend <enable>`  
启用发送安全设置建议。可以使用 [set](#safety-set) 命令设置选项。

### safety set
用法：`/cpm safety set <选项> <值>`
设置推荐的安全设置

## kick
控制将没有安装Mod的用户踢出。
子命令：
* [enable](#kick-enable)
* [disable](#kick-disable)
* [message](#kick-message)

### kick enable
用法：`/cpm kick enable <踢出时间>`
踢出时间（以ticks为单位）之后踢出没有安装 mod 的用户。

### kick disable
用法：`/cpm kick disable`
禁用踢出功能

### kick message
用法：`/cpm kick message <踢出消息>`
设置踢出消息

## scaling
用法：`/cpm scaling <缩放选项>`  
子命令：
* [limit](#scaling-limit)
* [enabled](#scaling-enabled)
* [method](#scaling-method)
* [reset](#scaling-reset)

### scaling limit
用法：`/cpm scaling <缩选项> limit [玩家] <最大>` 或者 `/cpm scaling <缩放选项> limit [玩家] <最小> <最大>`  
设置“缩放选项”的缩放限制。 可以选择使用`玩家`参数为每个玩家单独设置，对于全局设置留空。

### scaling enabled
使用：`/cpm scaling <缩放选项> enabled [玩家] <enable>`  
启用“缩放选项”的缩放。 可以选择使用`玩家`参数为每个玩家单独设置，对于全局设置留空。

### scaling method
使用：`/cpm scaling <缩放选项> method [玩家] <方法>`  
设置“缩放选项”的缩放方法。  
支持的选项：
* `disable` 方法的作用就像未安装缩放Mod一样。
* `default` 使用服务器默认的缩放器。
* `attribute` 使用原版属性
* `pehkui` 使用 [Pehkui](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling#pehkui) 进行缩放（安装后）
* 有关其他支持的模组，请查看[缩放页面](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling)。

### scaling reset
使用：`/cpm scaling reset <玩家>`  
重置选定玩家的缩放比例。

## animate
使用：`/cpm animate <类型> <动画名称> [值（0-255）]`  
为目标玩家播放动画。
值：0：重置姿势/手势，1：播放姿势/手势，对于图层值：0-255，切换：0-1 或留空以切换状态。

## effects
使用：`/cpm effects <效果> <选项>`  
可用效果：  
- `invisible_glow <true/false>`：为隐形玩家启用发光层渲染。默认开启。重新登录后才有效。

## detect
使用：`/cpm detect <类型> <动画名称> [值（0-255）]`  
检测指定的动画是否正在播放
如果存在`值`参数，则该命令检查动画值是否等于该参数
