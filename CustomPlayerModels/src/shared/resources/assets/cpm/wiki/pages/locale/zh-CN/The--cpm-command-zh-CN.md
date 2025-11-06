
<a name="the-cpm-command"/>

# `/cpm` 命令

> [!NOTE]
> 这是服务器端 CPM 命令，客户端命令请查看 `/cpm client`，详情见[这里](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpmclient-command-zh-CN)  


<a name="subcommands"/>

### 子命令：
* [setskin](#setskin)
* [safety](#safety)
* [kick](#kick)
* [scaling](#scaling)
* [animate](#animate)
* [effects](#effects)
* [detect](#detect)


<a name="setskin"/>

## setskin
用法：`/cpm setskin [-f|-t|-r] <目标> <base64 模型>`  
参数：
* -f：强制设定模型，玩家无法更改自己的模型。
* -t：临时模型，玩家退出游戏时会清除该模型。
* -r：重置模型，移除服务器设置的模型。

目标：要设置模型的玩家  
Base64模型：使用 Base64 选项导出的模型。  
详情请见：[Base64 导出](https://github.com/tom5454/CustomPlayerModels/wiki/Exporting-zh-CN#base64)


<a name="safety"/>

## safety
子命令：
* [recommend](#safety-recommend)
* [set](#safety-set)


<a name="safety-recommend"/>

### safety recommend
用法：`/cpm safety recommend <enable>`  
启用发送安全设置建议。可使用 [set](#safety-set) 命令配置具体选项。


<a name="safety-set"/>

### safety set
用法：`/cpm safety set <选项> <值>`  
设置推荐的安全配置。


<a name="kick"/>

## kick
控制是否踢出未安装Mod的玩家。  
子命令：
* [enable](#kick-enable)
* [disable](#kick-disable)
* [message](#kick-message)


<a name="kick-enable"/>

### kick enable
用法：`/cpm kick enable <踢出时间>`  
未安装Mod的玩家在指定时间（tick单位）后将被踢出。


<a name="kick-disable"/>

### kick disable
用法：`/cpm kick disable`  
禁用踢出功能。


<a name="kick-message"/>

### kick message
用法：`/cpm kick message <踢出消息>`  
设置踢出提示消息。


<a name="scaling"/>

## scaling
> [!NOTE]
> 本章可能已过时。
> 查看本页的英文版本：[跳转](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command#scaling)

用法：`/cpm scaling <缩放选项>`  
子命令：
* [limit](#scaling-limit)
* [enabled](#scaling-enabled)
* [method](#scaling-method)
* [reset](#scaling-reset)
* [debug](#scaling-debug)


<a name="scaling-limit"/>

### scaling limit
### scaling limit
用法：  
`/cpm scaling <缩放选项> limit [玩家] <最大>`  
或  
`/cpm scaling <缩放选项> limit [玩家] <最小> <最大>`

设置“缩放选项”的缩放限制。可选为单个玩家设置，留空则为全局设置。


<a name="scaling-enabled"/>

### scaling enabled
用法：`/cpm scaling <缩放选项> enabled [玩家] <启用>`  
启用“缩放选项”的缩放功能。可选为单个玩家设置，留空则为全局设置。


<a name="scaling-method"/>

### scaling method
用法：`/cpm scaling <缩放选项> method [玩家] <方法>`  
设置“缩放选项”的缩放方法。  
支持方法：  
* `disable`：禁用缩放，效果如同未安装缩放Mod。  
* `default`：使用服务器默认缩放器。  
* `attribute`：使用原版属性。  
* `pehkui`：使用 [Pehkui](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling-zh-CN#pehkui) 进行缩放（需安装）。  
详情请见[缩放页面](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling-zh-CN)。


<a name="scaling-reset"/>

### scaling reset
用法：`/cpm scaling reset <玩家>`  
重置指定玩家的缩放设置。


<a name="scaling-debug"/>

### 缩放调试
用法：`/cpm scaling debug <玩家>`  
打印玩家所有缩放设置，请求比例（Rq）、实际比例（V）、限制（Lim）、缩放方法（M）。

示例输出：
``` plaintext
<玩家> 的缩放调试：

- “实体缩放” M: 属性，V：1.5（Rq: 1.5），Lim：0.01 - 3.0
- “眼睛高度” M: 不支持（Rq: 1.0）
- “跳跃高度” M: 属性，V: 1.0（Rq: 1.0），Lim：0.01 - 10.0
- “生命值” M：缩放限制为正常值（Rq: 1.0）
```


<a name="animate"/>

## animate
用法：`/cpm animate <目标> <动画名称> [值（0-255）]`  
为目标玩家播放动画。

值说明：  
0：重置姿势/动作  
1：播放姿势/动作

图层值：0-255  
切换：0-1 或留空以切换状态。


<a name="effects"/>

## effects
用法：`/cpm effects <效果> <选项>`  
可用效果：  
* `invisible_glow <true/false>`：为隐形玩家启用发光层渲染，默认开启。重新登录后生效。  


<a name="detect"/>

## detect
用法：`/cpm detect <目标> <动画名称> [值（0-255）]`  
检测指定动画是否正在播放。  
若存在值参数，则检测动画值是否等于该参数。
