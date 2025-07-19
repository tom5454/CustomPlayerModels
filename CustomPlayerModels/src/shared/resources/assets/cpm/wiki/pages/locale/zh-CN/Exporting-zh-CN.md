
<a name="exporting-models"/>

## 导出模型

有~~三种~~方法可以导出你的模型：
* [保存在皮肤中](#stored-in-skin)  
* [模型文件](#model-file)  
* [Base64（仅适用于开发者，不适用于普通玩家）](#base64)  


<a name="stored-in-skin"/>

### 保存在皮肤中
模型将被存储在你的 Minecraft 皮肤中未被使用的区域。  
此方式允许你在原版服务器或未安装模组的服务器中使用模型，  
但无法在游戏中切换模型。  
[更多信息见此](#skin)


<a name="model-file"/>

### 模型文件
模型将被保存为 `.cpmmodel` 文件，位于 `<你的 Minecraft 目录>/player_models` 文件夹中。  
此模式需要服务器安装了模组或插件，**无法在原版服务器上使用。**  
你可以在游戏中通过 `手势菜单/模型` 切换你的模型。  
[更多信息见此](#exporting-as-local-model)


<a name="base64"/>

### Base64
Base64 模型适用于服务器管理员、地图制作者等高级用户。  
可以通过[命令](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command-zh-CN#setskin)或[API](https://github.com/tom5454/CustomPlayerModels/wiki/API-documentation-zh-CN#set-model)加载模型。  
此选项仅供开发者使用。


<a name="skin"/>

## 皮肤
要在游戏中使用自定义模型，您必须使用编辑器导出模型，然后上传导出的皮肤。

![图片：导出皮肤 GUI](https://github.com/tom5454/CustomPlayerModels/wiki/images/export_gui.png)

点击“...”按钮以选择输出文件，然后点击“导出”。  
你可以使用“更改原版皮肤”按钮更换基础皮肤。  
如果你没有安装该模组，游戏将显示这张皮肤。默认情况下，它会加载你当前使用的皮肤。  


<a name="data-overflow"/>

### 数据溢出
如果模型体积过大，无法写入皮肤文件中未使用的区域，你需要将它上传至以下平台之一：

- **我的粘贴站点**：选择“上传粘贴”。你可以更改粘贴名称。你可以在“编辑/粘贴”中查看上传的粘贴，网站限制：每个模型最多粘贴 15 次，每个文件最大 100kB。
- **[GitHub Gist](https://gist.github.com/)**：选择“上传 Gist”，点击“复制”以复制模型数据，然后创建新的 Gist 或 Pastebin 并粘贴。  
  复制生成的 URL 并粘贴到底部的文本框中。
- **GitHub 仓库**：你可以将模型数据写入一个公开仓库中的文本文件，然后提供该文件的链接。

> [!NOTE]
> 仅当你拥有活跃的 GitHub 帐号时才建议使用 Gist。  
> 加入 [CPM Discord 服务器](https://discord.gg/mKyXdEsMZD)，在 `#commands` 频道运行 `/paste-register` 来扩展你的粘贴上传限制。  
> 使用我的粘贴站点前，你需要拥有一个有效的 Minecraft 帐号！

![图片：导出溢出 Gui](https://github.com/tom5454/CustomPlayerModels/wiki/images/export_overflow_popup.png)

按“确定”完成导出。

导出后，使用您的 Minecraft 启动器或 Minecraft 网站将导出的皮肤文件上传为您的 Minecraft 皮肤。  
模型数据被写入皮肤上未使用的空间，因此如果您没有安装此模组，模型将不会显示。  
如果启动器显示无法加载皮肤，请尝试重新启动启动器。

在服务器上安装了该模组的用户都可以加载此模型。


<a name="exporting-as-local-model"/>

## 导出为本地模型
> [!NOTE]
> 此功能需要服务器已安装 CPM 模组。

你可以将模型导出为本地模型（`.cpmmodel` 文件）。  
在安装了该模组的服务器或单人游戏中，即使在游戏中，你也可以通过 `手势菜单/模型` 更换模型。  

![图片：导出模型 Gui](https://github.com/tom5454/CustomPlayerModels/wiki/images/export_model.png)

打开菜单 `文件/导出`，点击“导出方式：???” 按钮，切换为“导出：模型”。  
导出的文件名将作为模型名称。你还可以为模型设置图标。  
点击“导出”按钮完成导出。

你可以在“编辑/模型”或游戏中的“手势菜单/模型”中应用该模型。  
选择模型后，点击预览下方的“应用”按钮。

本地模型**仅适用于安装了该模组或插件的单人游戏与多人服务器中。**  
[如果模型太大（超过 30kB），你必须上传](#data-overflow)  

你也可以在编辑器主界面（需从游戏主菜单打开）中的“编辑/模型”菜单中，将兼容皮肤的模型转换为[保存在皮肤中](#stored-in-skin)的模型。  

更多内容请见：[模型弹出窗口](https://github.com/tom5454/CustomPlayerModels/wiki/Models-Menu-zh-CN#models-popup)
