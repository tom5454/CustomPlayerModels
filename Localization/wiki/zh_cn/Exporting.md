
<a name="exporting-models"/>

## 导出模型

有~~三种~~方法可以导出你的模型：
* [保存在皮肤中](#stored-in-skin)  
* [模型文件](#model-file)  
* [Base64（仅适用于开发者，不适用于普通玩家）](#base64)  


<a name="stored-in-skin"/>

### 保存在皮肤中
模型将存储在你的 Minecraft 皮肤未使用的区域中。
此方式可以让你在原版服务器和没有此模组的服务器上使用。 
但是您无法在游戏中改变您的模型。
[更多信息如下](#skin)


<a name="model-file"/>

### 模型文件
该模型将存储在 `<你的 Minecraft 目录>/player_models` 内的 `.cpmmodel` 文件中。
此模式需要在服务器上安装模组或插件，并且无法在原版服务器上使用。 
您可以在游戏中在`手势菜单/模型`下更改您的模型。
[更多信息如下](#exporting-as-local-model)


<a name="base64"/>

### Base64
Base64 模型适用于服务器所有者、地图制作者。
可以[使用命令](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command#setskin)或[API](https://github.com/tom5454/CustomPlayerModels/wiki/API-documentation#set-model)来加载模型。
此选项仅适用于开发人员。 


<a name="skin"/>

## 皮肤
要在游戏中使用自定义模型，您必须使用编辑器导出模型，然后上传导出的皮肤。

![图片：导出皮肤 GUI](https://github.com/tom5454/CustomPlayerModels/wiki/images/export_gui.png)

单击“...”按钮设置输出文件，然后点击“导出”。您可以使用“更改原版皮肤”按钮更改基本皮肤。如果您未安装该模组，则会显示此皮肤，默认情况下它会加载您当前的皮肤。
皮肤层设置用于[创建自定义动画]（https://github.com/tom5454/CustomPlayerModels/wiki/Animations#custom-animations-encoding）。


<a name="data-overflow"/>

### 数据溢出
如果模型太大，无法放入皮肤文件中未使用的空间，那么您必须将其上传到以下位置之一：
- 我的粘贴站点：选择“上传粘贴”。您可以更改粘贴的名称（您可以在编辑/粘贴中查看已上传的粘贴，站点限制每个文件最多可粘贴 15 次，最多 100kB）。
- [GitHub Gist](https://gist.github.com/)：选择“上传 Gist”选项。单击“复制”，然后创建一个新的 Gist/Pastebin 并粘贴您刚刚复制的模型数据。然后从创建的 Gist 中获取 URL，然后将其放入底部的文本字段中。
- GitHub存储库，您​​可以将模型数据放入公开的存储库中的文本文件中并链接它。

> [!NOTE]
> 仅当您拥有活跃的 GitHub 帐户时才使用 GitHub Gists！
> 加入 [CPM discord 服务器](https://discord.gg/mKyXdEsMZD) 并在 #commands 频道中运行 `/paste-register` 以扩展您的粘贴存储限制
> 您需要一个有效的 Minecraft 帐户才能使用我的粘贴网站！

![图片：导出溢出 Gui](https://github.com/tom5454/CustomPlayerModels/wiki/images/export_overflow_popup.png)

按“确定”完成导出。

导出后，使用您的 Minecraft 启动器或 Minecraft 网站将导出的皮肤文件上传为您的 Minecraft 皮肤。
模型数据被写入皮肤上未使用的空间，因此如果您没有安装此模组，模型将不会显示。
如果启动器显示无法加载皮肤，请尝试重新启动启动器。

在服务器上安装了该模组的用户都可以加载此模型。


<a name="exporting-as-local-model"/>

## 导出为本地模型
> [!NOTE]
> 此功能需要在服务器上安装 CPM。

您可以将模型导出为本地模型。在安装了自定义玩家模型模组的服务器上或在单人游戏中，甚至在游戏中，您可以在手势菜单/模型中更改这些模型。
![图片：导出模型 Gui](https://github.com/tom5454/CustomPlayerModels/wiki/images/export_model.png)  
您可以在“文件/导出”中导出本地模型，然后点击“导出方式：???”按钮至“导出：模型”。
模型名称将为文件名。您可以为模型设置一个图标。
点击“导出”。
在“编辑/模型”或游戏中的“手势菜单/模型”中应用您的模型。
选择您的模型并单击预览下方的应用。
本地模型仅适用于安装了模组或插件的单人游戏和多人服务器。 
[如果模型太大（超过 30kB），则必须上传](#data-overflow)  
可以使用编辑器中的“编辑/模型”菜单将皮肤兼容模型转换为常规[存储在皮肤中](#stored-in-skin)的模型(您必须从主菜单打开编辑器)。
查看：[模型弹出窗口](https://github.com/tom5454/CustomPlayerModels/wiki/Models-Menu#models-popup)  
