
<a name="q-forge-or-fabric"/>

### 问题：Mod 是 Forge 还是 Fabric？
回答：是的
[CF上的Forge](https://www.curseforge.com/minecraft/mc-mods/custom-player-models)  
[CF上的Fabric/Quilt](https://www.curseforge.com/minecraft/mc-mods/custom-player-models-fabric)  
[CF上的Bukkit/Spigot/Paper](https://www.curseforge.com/minecraft/bukkit-plugins/custom-player-models-bukkit)  
[GitHub上的Bukkit/Spigot/Paper](https://github.com/tom5454/CustomPlayerModels/releases)  
[Modrinth上的Forge，Fabric 或 Quilt](https://modrinth.com/mod/custom-player-models)


<a name="q-discord"/>

### 问题：有 Discord服务器 吗？
回答：[https://discord.gg/mKyXdEsMZD](https://discord.gg/mKyXdEsMZD)


<a name="q-does-the-mod-work-in-servers"/>

### 问题：这个模组可以在服务器中使用吗？
回答：这是一个客户端模组。  
这意味着只要你安装了这个模组，就可以看到其他人的模型。  
请查看：[导出指南](https://github.com/tom5454/CustomPlayerModels/wiki/Exporting)


<a name="q-does-this-projectmodel-work-on-my-minecraft-version"/>

### 问题：该项目/模型适用于我的 Minecraft 版本吗？
回答：是的，项目和模型兼容所有受支持的 Minecraft 版本。


<a name="q-where-is-the-model-editor"/>

### 问题：模型编辑器在哪里？
回答：它位于游戏主菜单左上角，  
你不需要下载任何第三方程序。


<a name="q-how-do-i-use-my-model"/>

### 问题：我要如何使用我的模型？
回答：你需要导出你的项目。请查看：[导出指南](https://github.com/tom5454/CustomPlayerModels/wiki/Exporting)


<a name="q-how-do-i-make-a-model"/>

### 问题：我要如何制作模型？
回答：建议先使用“转换原版部件”工具，它可以直接让你编辑角色肢体，  
然后再添加部件、细节和纹理，直到完成。  
参考：[Discord 的 #community-tutorials 频道](https://discord.com/channels/811508670205788211/844561618281168968) 或  
[CPM 综合用户指南](https://docs.google.com/presentation/d/117GBWxtyNT6L3a69cENXz1Gjx-MplB1UC9l_uISn6oQ/edit?usp=sharing)（Google文档，作者：Discord 的 `@w3eb`）

<a name="q-where-can-i-download-models"/>

### 问题：哪里可以下载模型？
回答：请查看 [Discord 上的 #free-models 频道](https://discord.com/channels/811508670205788211/811532237521551360)。


<a name="q-how-do-i-color-or-texture"/>

### 问题：如何设置颜色或者纹理？
回答：你可以在左侧面板中进行修改。  
但如果使用第三方软件（如 paint.net、GIMP 或 Photoshop）来编辑会更方便。  
你还可以调整纹理表的大小。

<a name="q-how-do-you-make-more-shapes"/>

### 问题：我要如何制作更多形状？
回答：你只能使用“块”来组合制作出复杂形状，没有其他方法能实现制作复杂形状。  
例如，你可以用两个相同颜色的方块拼出一个三角形。

<a name="q-how-do-i-animate"/>

### 问题：我要如何制作动画？
回答：通过调整不同姿势，并设置毫秒数值来控制动作之间的过渡时长。你还可以设置部件在动画过程中的显示与隐藏！


<a name="q-how-do-i-export"/>

### 问题：我要如何导出？
回答：点击“导出”按钮，并选择一个原版玩家可见的皮肤（即未安装 CPM 玩家看到的默认外观）。
随后选择保存路径并命名；若文件名重复或包含非法字符，将无法成功导出。

<a name="q-what-is-a-gist"/>

### 问题：Gist 是什么？
回答：你可以通过 <https://gist.github.com/> 来创建它。
你需要登录 GitHub 账号，模组会生成一段数据供你复制并粘贴到 Gist 中。创建完成后，将该 Gist 链接复制并粘贴回编辑器即可。

之所以需要这样做，是因为模型数据量太大，无法直接全部存储在皮肤文件内，必须通过这种方式进行外部托管。

<a name="qare-there-any-tutorials"/>

### 问题：有教程吗？
回答：请参考 [Discord 上的 #community-tutorials 频道](https://discord.com/channels/811508670205788211/844561618281168968)，  
或查看 [CPM 综合用户指南](https://docs.google.com/presentation/d/117GBWxtyNT6L3a69cENXz1Gjx-MplB1UC9l_uISn6oQ/edit?usp=sharing)（作者：Discord 的 @w3eb）


<a name="q-how-do-i-make-a-part-glow"/>

### 问题：如何让部件发光？
回答：通过应用“发光的眼睛”效果，你可以让整个部件或方块像末影人眼睛那样不受光照影响。
由于该效果会使部件表现得像透明层（只显示发光部分），解决方法是：在完全相同的位置放置两个重合的部件，其中一个保持常规渲染，另一个应用“发光的眼睛”效果。
这样就能实现既有实心纹理、又有发光效果的部件了。
（如果你安装了光影，该部件还会产生真实的辉光效果。）


<a name="q-how-do-i-stop-using-the-vanilla-animations"/>

### 问题：如何禁用原版动画？
回答：关闭“叠加 (Additive)”模式，然后微调一下根部肢体的位置，即可开始制作你自己的动画。
（注意：这不适用于你从原版部件转换而来的子零件。）
此外，你也可以直接应用“禁用原版动画”效果。


<a name="q-its-not-showing-up-in-game"/>

### 问题：模型在游戏中没有显示！
回答：请确认你是否使用了正确的皮肤。
检查皮肤文件上是否有模型数据（即皮肤边缘那些杂乱的颜色块等）。
由于离线/盗版服务器无法加载皮肤，请先在单人模式下进行测试。


<a name="q-can-someone-make-me-a-model"/>

### 问题：有人可以帮我制作模型吗？
回答：除非有人主动提出，否则**不要要求别人免费帮你制作模型！！！**。


<a name="q-how-to-make-fancy-legs"/>

### 问题：如何制作好看的腿？
回答：以特定方式排列“块”来制作特殊腿型。  
许多人常用做的双足（bipedal）结构的腿型。


<a name="q-how-do-i-make-a-tail"/>

### 问题：我要如何制作尾巴？
回答：添加一系列“块”并使其延长即可。  
你可以在 YouTube 上找到相关视频教程。


<a name="q-can-i-have-that-model-"/>

### 问题：我可以使用那个模型吗？
回答：请查看 [Discord 上的 #free-models 频道](https://discord.com/channels/811508670205788211/811532237521551360)。  
如果某个模型还没发布，或作者不打算分享，那就无法获取。


<a name="q-setting-player-model-type-slim-or-classic-base-model"/>

### 问题：如何设置玩家模型类型（苗条或经典）？
回答：依次点击 文件 → 新建 → 新建模型，然后选择“苗条（爱丽克丝）”或“默认（史蒂夫）”。


<a name="q-what-is-the-bukkit-plugin-for"/>

### 问题：Bukkit 插件是做什么用的？
回答：这个插件相当于在服务器上安装了该模组。  
它允许你在服务器中通过模型菜单快速修改模型。


<a name="q-changing-your-model-on-the-fly"/>

### 问题：如何快速切换模型？
回答：先将模型导出为模型文件，然后在动作菜单（按 G）中的模型菜单中进行切换。  
请注意，服务器也必须安装了该模组。


<a name="q-how-to-use-the-skin-changer"/>

### 问题：如何使用皮肤更换器？
回答：导出时可以选择“导出并上传”，  
也可以在编辑器中的“编辑 / 模型”下选择模型后点击“上传皮肤”。  
请注意，这仅在你不在游戏中时才有效。


<a name="q-can-i-use-blockbench"/>

### 问题：我可以使用 Blockbench 吗？
回答：可以，并且有专用的插件：[使用 Blockbench](https://github.com/tom5454/CustomPlayerModels/tree/master/Blockbench)
