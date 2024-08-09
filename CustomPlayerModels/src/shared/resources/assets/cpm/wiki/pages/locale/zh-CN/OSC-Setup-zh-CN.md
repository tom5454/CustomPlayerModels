所需插件模组：[CurseForge 的自定义玩家模型OSC兼容](https://www.curseforge.com/minecraft/mc-mods/cpmoscc)、[Modrinth上的自定义玩家模型OSC兼容](https://modrinth.com/mod/cpmoscc) 


<a name="setting-up-osc-triggered-animations-with-cpm"/>

## 通过 CPM 设置 OSC 触发动画
您必须在`编辑/工具/OSC 设置`下配置 OSC。  
使用`编辑/工具/OSC动画向导`创建OSC触发动画。
动画必须是`命令激活动画`。
使用`值层`动画类型将 OSC 值映射到 CPM 动画。 
切换图层在 0.5 以上触发（映射）
值映射：
CPM 需要标准化值 (0-1)，使用动画向导设置最小值和最大值。
`值映射 = (OSC值 - minimum最小值_value) / (最大值 - 最小值)`  

CPM 与 OSC/VMC 值兼容。


<a name="scripting"/>

## 脚本编写
您可以使用 [TouchOSC](https://hexler.net/touchosc#get) 运行响应 OSC 命令的 lua 脚本。 [TouchOSC 脚本编写示例](https://hexler.net/touchosc/manual/script-examples)
OSC 的 Python 库：[python-osc](https://github.com/attwad/python-osc)


<a name="other-applications"/>

## 其他应用程序
人脸追踪：[VSeeFace](https://www.vseeface.icu/)
大多数 OSC 发送应用程序都可以与 VRChat 一起使用。 [VRChat OSC 资源](https://docs.vrchat.com/docs/osc-resources)
