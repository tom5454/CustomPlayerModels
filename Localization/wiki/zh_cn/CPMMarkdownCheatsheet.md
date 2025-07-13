这是根据 [Markdown Here](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet) 修改的副本，  
包含了 CPM 游戏内 Wiki 查看器支持的所有功能。

本文旨在作为快速参考与演示。  
如需更完整的信息，请参阅 [John Gruber 的原始规范](http://daringfireball.net/projects/markdown/)  
以及 [GitHub 风格 Markdown 的说明页面](http://github.github.com/github-flavored-markdown/)。


<a name="table-of-contents"/>

##### 目录
[标题](#headers)  
[强调](#emphasis)  
[列表](#lists)  
[链接](#links)  
[图片](#images)  
[代码与语法高亮](#code)  
[脚注](#footnotes)  
[表格](#tables)  
[引用块](#blockquotes)  
[分隔线](#hr)  
[换行符](#lines)  
[YouTube 视频嵌入](#videos)  
[高亮 / 提示框](#highlighting)

<a name="headers"/>

## 标题

```no-highlight
<a name="custom-name"/>


<a name="h1"/>

# H1

<a name="h2"/>

## H2

<a name="h3"/>

### H3

<a name="h4"/>

#### H4

<a name="h5"/>

##### H5

<a name="h6"/>

###### H6

[跳转到H1](#custom-name)
```

<a name="custom-name"/>


<a name="h1"/>

# H1

<a name="h2"/>

## H2

<a name="h3"/>

### H3

<a name="h4"/>

#### H4

<a name="h5"/>

##### H5

<a name="h6"/>

###### H6

[H1跳转到](#custom-name)

<a name="emphasis"/>

## Emphasis

```no-highlight
强调，也就是斜体，可以使用 *星号* 或 _下划线_。

强烈强调，也就是粗体，可以使用 **双星号** 或 __双下划线__。

组合强调可以使用 **星号加上下划线 _组合使用_**。

删除线使用两个波浪号。~~划掉这个。~~
```

强调，也就是斜体，可以使用 *星号* 或 _下划线_。

强烈强调，也就是粗体，可以使用 **双星号** 或 __双下划线__。

组合强调可以使用 **星号加上下划线 _组合使用_**。

删除线使用两个波浪号。~~划掉这个。~~


<a name="lists"/>

## Lists

（在此示例中，前导空格和尾随空格用点表示：⋅）

```no-highlight
1. 第一项有序列表
2. 另一项
  * 无序子列表
1. 实际数字很重要，这和规范不同。
  1. 有序子列表
4. 还有一项。

   你可以在列表项中有正确缩进的段落。注意上方的空行和前导空格（至少一个，这里用三个以对齐原始 Markdown）。

   如果想要换行但不分段落，需要在行尾加两个空格。  
   注意这一行是分开的，但仍在同一个段落内。  
   （这和常见的 GFM 换行行为不同，GFM 不要求尾部空格。）

* 无序列表可以用星号
- 或者减号
+ 也可以用加号

```

1. 第一项有序列表
2. 另一项
  * 无序子列表
1. 实际数字很重要，这和规范不同。
  1. 有序子列表
4. 还有一项。

   你可以在列表项中有正确缩进的段落。注意上方的空行和前导空格（至少一个，这里用三个以对齐原始 Markdown）。

   如果想要换行但不分段落，需要在行尾加两个空格。  
   注意这一行是分开的，但仍在同一个段落内。  
   （这和常见的 GFM 换行行为不同，GFM 不要求尾部空格。）

* 无序列表可以用星号
- 或者减号
+ 也可以用加号


<a name="links"/>

## Links

有两种方法可以创建链接。

```no-highlight
[我是一个内联样式链接](https://www.google.com)

[我是带标题的内联样式链接](https://www.google.com "Google 首页")

[我是引用样式链接][任意大小写不敏感的引用文本]

[我是指向仓库文件的相对引用链接](../blob/master/LICENSE)

[你也可以用数字作为引用样式链接定义][1]

[文本本身] 是不起作用的。

这里有一些文本用来说明引用链接可以写在后面。

[任意大小写不敏感的引用文本]: https://www.mozilla.org
[1]: http://slashdot.org

```

[我是一个内联样式链接](https://www.google.com)

[我是带标题的内联样式链接](https://www.google.com "Google 首页")

[我是引用样式链接][任意大小写不敏感的引用文本]

[我是指向仓库文件的相对引用链接](../blob/master/LICENSE)

[你也可以用数字作为引用样式链接定义][1]

[文本本身] 是不起作用的。

这里有一些文本用来说明引用链接可以写在后面。

[任意大小写不敏感的引用文本]: https://www.mozilla.org
[1]: http://slashdot.org


<a name="images"/>

## Images

所有图片必须上传到 wiki repo。
请将您的图片放入“Localization/wiki/images”目录，如果可以，请重复使用现有图片。

```no-highlight
这是一个旋转器图标（悬停查看标题文字）：

内联样式：  
![替代文字](https://github.com/tom5454/CustomPlayerModels/wiki/images/spinner.png "旋转图标文本 1")

引用样式：  
![替代文字][logo]

[logo]: https://github.com/tom5454/CustomPlayerModels/wiki/images/spinner.png "旋转图标文本 2"
```

这是一个旋转器图标（悬停查看标题文字）：

内联样式：  
![替代文字](https://github.com/tom5454/CustomPlayerModels/wiki/images/spinner.png "旋转图标文本 1")

引用样式：  
![替代文字][logo]

[logo]: https://github.com/tom5454/CustomPlayerModels/wiki/images/spinner.png "旋转图标文本 2"

<a name="code"/>

## 代码和语法高亮

代码块是 Markdown 规范的一部分，但语法高亮不是。不过，许多渲染器（例如 Github 的渲染器）都支持语法高亮。至于支持哪些语言以及这些语言名称的书写方式，各个渲染器会有所不同。

```no-highlight
内联 `代码` 用反引号括起来。
```

内联 `代码` 用反引号括起来。

代码块由三条反引号 '\`\`\`' 围起来。

```
\```java
public static void test() {
    String s = "Java 语法高亮，仅限 GitHub";
    System.out.println(s);
}
\```

\```
没有指定语言，所以没有语法高亮。  
但我们可以插入一个 <b>tag</b>。
\```
```

```java
public static void test() {
    String s = "Java 语法高亮";
    System.out.println(s);
}
```

```
没有指定语言，所以没有语法高亮。  
但我们可以插入一个 <b>tag</b>。
```

<a name="footnotes"/>

## 脚注

表格不属于 Markdown 核心规范的一部分，但它是 GFM 的一部分，并且 CPM Wiki Viewer 也支持表格。表格是向 Wiki 页面添加表格的简便方法。

```no-highlight
这里有一个简单的脚注[^1]。

脚注也可以有多行[^2]。

你还可以用单词作为脚注标记，更符合你的写作风格[^note]。

[^1]: 我的引用。
[^2]: 每一新行前应加两个空格。  
  这样就可以有多行的脚注。
[^note]:
    命名脚注显示时仍用数字代替文本，但便于识别和链接。  
    此脚注使用不同语法，新行前加四个空格。
```

这里有一个简单的脚注[^1]。

脚注也可以有多行[^2]。

你还可以用单词作为脚注标记，更符合你的写作风格[^note]。

[^1]: 我的引用。
[^2]: 每一新行前应加两个空格。  
  这样就可以有多行的脚注。
[^note]:
    命名脚注显示时仍用数字代替文本，但便于识别和链接。  
    此脚注使用不同语法，新行前加四个空格。


<a name="tables"/>

## 表格

表格不是核心 Markdown 规范的一部分，但它们属于 GFM，CPM Wiki 查看器也支持。表格是给你的 wiki 页面添加表格的简便方式。

```no-highlight
冒号可以用来对齐列。

| 表格          | 是            | 很酷  |
| ------------- |:-------------:| -----:|
| 第3列内容     | 右对齐        | $1600 |
| 第2列内容     | 居中          |   $12 |
| 斑马纹样式   | 很漂亮        |    $1 |

每个表头单元格之间必须至少有3个短横线。
外侧的竖线（|）是必须的，且你不需要让原始 Markdown 排版整齐。你也可以使用内联 Markdown。

| Markdown | 更少 | 更漂亮 |
| --- | --- | --- |
| *依然* | `渲染` | **漂亮** |
| 1 | 2 | 3 |
```

冒号可以用来对齐列。

| 表格        |   是   |  很酷 |
| ----------- | :----: | ----: |
| 第 3 列内容 | 右对齐 | $1600 |
| 第 2 列内容 |  居中  |   $12 |
| 斑马纹样式  | 很漂亮 |    $1 |

每个表头单元格之间必须至少有 3 个短横线。外侧的竖线（|）是必须的，且你不需要让原始 Markdown 排版整齐。你也可以使用内联 Markdown。

| Markdown | 更少   | 更漂亮   |
| -------- | ------ | -------- |
| _依然_   | `渲染` | **漂亮** |
| 1        | 2      | 3        |


<a name="blockquotes"/>

## Blockquotes

```no-highlight
> 块引用非常方便
> 这句话是同一句引言的一部分。

引文中断。

> 这行代码很长，换行后仍然能正确引用。哦天哪，我们继续写吧，确保它足够长，方便所有人换行。哦，你可以把 **Markdown** 放进块引用里。
```

> 块引用非常方便
> 这句话是同一句引言的一部分。

引文中断。

> 这行代码很长，换行后仍然能正确引用。哦天哪，我们继续写吧，确保它足够长，方便所有人换行。哦，你可以把 **Markdown** 放进块引用里。

<a name="hr"/>

## 分隔线

```
三个或更多...

---

连字符

***

星号

___

下划线
```

三个或更多...

---

连字符

***

星号

___

下划线


<a name="lines"/>

## Line Breaks

我学习换行符工作原理的基本建议是多尝试和探索——按一次 &lt;Enter&gt;（即插入一个换行符），然后按两次（即插入两个换行符），看看会发生什么。你很快就会学会如何操作。“Markdown Toggle” 是你的好帮手。

以下是一些可以尝试的方法：

```
我们从这行开始。

这一行与上一行之间有两个换行符，因此它将是一个*独立段落*。

这一行也是一个独立段落，但是……
这一行仅由一个换行符分隔，因此它是*同一段落*中的独立行。
```

我们从这行开始。

这一行与上一行之间有两个换行符，因此它将是一个*独立段落*。

这一行也是一个独立段落，但是……
这一行仅由一个换行符分隔，因此它是*同一段落*中的独立行。

<a name="videos"/>

## YouTube 视频

它们不能直接添加，但你可以添加带有视频链接的图片，像这样：

```no-highlight
[![这里是图片替代文字](https://github.com/tom5454/CustomPlayerModels/wiki/images/spinner.png)](http://www.youtube.com/watch?v=YOUTUBE_VIDEO_ID_HERE)
```

[![这里是图片替代文字](https://github.com/tom5454/CustomPlayerModels/wiki/images/spinner.png)](http://www.youtube.com/watch?v=dQw4w9WgXcQ)

查看 [图像](#images) 了解如何加载自定义图片。


<a name="highlighting"/>

## 高亮 / 提示

本节内容摘自 [GitHub 文档](https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#alerts)

```
> [!NOTE]
> Useful information that users should know, even when skimming content.

> [!TIP]
> Helpful advice for doing things better or more easily.

> [!IMPORTANT]
> Key information users need to know to achieve their goal.

> [!WARNING]
> Urgent info that needs immediate user attention to avoid problems.

> [!CAUTION]
> Advises about risks or negative outcomes of certain actions.

```

> [!NOTE]
> Useful information that users should know, even when skimming content.

> [!TIP]
> Helpful advice for doing things better or more easily.

> [!IMPORTANT]
> Key information users need to know to achieve their goal.

> [!WARNING]
> Urgent info that needs immediate user attention to avoid problems.

> [!CAUTION]
> Advises about risks or negative outcomes of certain actions.

---

来源许可：[CC-BY](https://creativecommons.org/licenses/by/3.0/)
