<a name="animated-textures"/>

## 动画纹理

通过添加动画纹理效果，可以让部分纹理实现动画播放。  
选择要添加动画的纹理，然后点击 `效果/添加动画纹理`。  
![模型树中的动画纹理](https://github.com/tom5454/CustomPlayerModels/wiki/images/animated_tree.png)

<a name="options"/>

### 选项

- **区域开始与区域大小**：设置动画区域在纹理上的起始坐标与大小。可使用 U/V 微调器进行精确调整。
- **动画纹理**：动画帧左上角在纹理上的 U/V 坐标。  
- **帧时间**：每帧持续的时间（毫秒）。
- **帧数**：动画包含的帧数。
- **水平**：动画帧的排列方式。
- **插值**：在帧之间进行插值。

![皮肤面板中的动画纹理](https://github.com/tom5454/CustomPlayerModels/wiki/images/animated_tex.png)  

黄色区域表示动画播放区域，白色区域为每一帧的纹理框架。  
示例项目：[链接](https://github.com/tom5454/CustomPlayerModels/raw/master/examples/animated_texture.cpmproject)  
动画播放区域不应与动画帧区域重叠，否则在游戏中可能出现显示异常。
