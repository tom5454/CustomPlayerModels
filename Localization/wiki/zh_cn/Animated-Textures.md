
<a name="animated-textures"/>

## 动画纹理

通过添加动画纹理效果，可以对部分纹理进行动画处理。  
选择要添加动画的纹理，然后按 `效果/添加动画纹理`  
![模型树中的动画纹理](https://github.com/tom5454/CustomPlayerModels/wiki/images/animated_tree.png)  


<a name="options"/>

### 选项

**区域开始和区域大小：**
设置纹理上的动画区域。这是纹理将被动画化的区域。
使用 U/V 微调器来获得该值。
**动画纹理：**
动画纹理帧的左上角 U/V。
**帧时间：**
每帧的时间（以毫秒为单位）。
**帧数：**
动画中的帧数。
**水平：**
动画帧的布局。
**插值：**
在帧之间进行插值。  
![皮肤面板中的动画纹理](https://github.com/tom5454/CustomPlayerModels/wiki/images/animated_tex.png)  
黄色区域将会有动画效果。 白色区域是动画框架。
示例项目：[链接]（https://github.com/tom5454/CustomPlayerModels/raw/master/examples/animated_texture.cpmproject）
动画区域不应与动画纹理帧区域重叠。这将导致游戏中出现视觉故障。
