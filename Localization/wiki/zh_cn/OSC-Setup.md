所需插件模组：自定义玩家模型OSC兼容  
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/cpmoscc) | [Modrinth](https://modrinth.com/mod/cpmoscc)


<a name="setting-up-osc-triggered-animations-with-cpm"/>

## 通过 CPM 设置 OSC 触发动画

您需要在 `编辑/工具/OSC 设置` 中配置 OSC。  
使用 `编辑/工具/OSC 动画向导` 创建由 OSC 触发的动画。  

动画必须为 **命令激活动画（Command Activated Animation）**。  

使用 **值层动画（Value Layer）** 类型，将 OSC 值映射到 CPM 动画上。  
切换图层会在值超过 `0.5` 时被触发。  

**值映射公式：**  
CPM 要求输入值为标准化的 `0-1` 范围。  
您可以通过动画向导设置“最小值”和“最大值”，并应用以下计算方式：
``` plaintext
映射值 = (OSC 原始值 - 最小值) / (最大值 - 最小值)
```

CPM 与 OSC/VMC 协议的值兼容。

<a name="scripting"/>

## 脚本编写
您可以使用 [TouchOSC](https://hexler.net/touchosc#get) 编写响应 OSC 命令的 Lua 脚本。  
参考示例：[TouchOSC 脚本编写示例](https://hexler.net/touchosc/manual/script-examples)

OSC 的 Python 库推荐使用：[python-osc](https://github.com/attwad/python-osc)


<a name="other-applications"/>

## 其他应用程序
人脸追踪工具推荐：[VSeeFace](https://www.vseeface.icu/)

大多数 OSC 发送应用程序也适用于 VRChat。  
参考资源：[VRChat OSC 资源](https://docs.vrchat.com/docs/osc-resources)
