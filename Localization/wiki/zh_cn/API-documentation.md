# CPM Api 文档
目录：
* [开始](#开始) ([Versions](#latest-versions))
* [创建你的插件](#创建你的插件)
* [客户端 API](#客户端-api)
* [通用 API](#通用-api)

## 开始
将 CPM api 添加到你的 gradle 构建脚本（`build.gradle`）：

### 手动
从 [Releases](https://github.com/tom5454/CustomPlayerModels/releases/tag/0.4.0a) 下载 api
并将其放入您的模组项目文件夹中。
将其添加到你的 gradle 文件中

```gradle
dependencies {
	implementation files("CustomPlayerModels-API-0.4.0.jar");
}
```

### Gradle

#### 存储库

```gradle
repositories {
  maven {
    name = "tom5454 maven"
    url = "https://raw.githubusercontent.com/tom5454/maven/main"
  }
}
```

#### 最新版本
API 版本： ![API 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-API%2Fmaven-metadata.xml)

| Minecraft 版本 | 运行版本（Forge） | 运行版本（NeoForge） | 运行版本（Fabric） | 运行版本（Quilt） |
| ------------- | --------------- | ------------------ | ---------------- | --------------- |
| 1.20.5 | ![1.20.5 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModelsLexForge-1.20.5%2Fmaven-metadata.xml) | ![1.20.5 neoForge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.20.5%2Fmaven-metadata.xml) | ![1.20.5 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.20.5%2Fmaven-metadata.xml) | - |
| 1.20.4 | ![1.20.4 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModelsLexForge-1.20.4%2Fmaven-metadata.xml) | ![1.20.4 neoForge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.20.4%2Fmaven-metadata.xml) | ![1.20.4 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.20.4%2Fmaven-metadata.xml) | ![1.20.4 quilt version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Quilt-1.20.4%2Fmaven-metadata.xml) |
| 1.20.2 | ![1.20.2 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModelsLexForge-1.20.2%2Fmaven-metadata.xml) | ![1.20.2 neoForge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.20.2%2Fmaven-metadata.xml) | ![1.20.2 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.20.2%2Fmaven-metadata.xml) | Use the Fabric version |
| 1.20 | ![1.20 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.20%2Fmaven-metadata.xml) | Use the Forge version | ![1.20 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.20%2Fmaven-metadata.xml) | ![1.20 quilt version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Quilt-1.20%2Fmaven-metadata.xml) |
| 1.19.4 | ![1.19.4 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.19.4%2Fmaven-metadata.xml) | - | ![1.19.4 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.19.4%2Fmaven-metadata.xml) | Use the Fabric version |
| 1.19.3 | ![1.19.3 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.19.3%2Fmaven-metadata.xml) | - | ![1.19.3 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.19.3%2Fmaven-metadata.xml) | Use the Fabric version |
| 1.19 | ![1.19 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.19%2Fmaven-metadata.xml) | - | ![1.19 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.19%2Fmaven-metadata.xml) | Use the Fabric version |
| 1.18 | ![1.18 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.18%2Fmaven-metadata.xml) | - | ![1.18 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.18%2Fmaven-metadata.xml) | Use the Fabric version |
| 1.17 | ![1.17 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.17%2Fmaven-metadata.xml) | - | ![1.17 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.17%2Fmaven-metadata.xml) | - |
| 1.16 | ![1.16 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.16%2Fmaven-metadata.xml) | - | ![1.16 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.16%2Fmaven-metadata.xml) | - |
| 1.15 | ![1.15 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.15%2Fmaven-metadata.xml) | - | ![1.15 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.15%2Fmaven-metadata.xml) | - |
| 1.14 | ![1.14 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.14%2Fmaven-metadata.xml) | - | ![1.14 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.14%2Fmaven-metadata.xml) | - |
| 1.12.2 | ![1.12 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.12.2%2Fmaven-metadata.xml) | - | - | - |
| 1.10.2 | ![1.10 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.10.2%2Fmaven-metadata.xml) | - | - | - |
| 1.8 | ![1.8 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.8%2Fmaven-metadata.xml) | - | - | - |
| 1.7.10 | ![1.7 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.7.10%2Fmaven-metadata.xml) | - | - | - |
| 1.6.4 | ![1.6 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.6.4%2Fmaven-metadata.xml) | - | - | - |
| 1.5.2 | ![1.5 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.5.2%2Fmaven-metadata.xml) | - | - | - |
| 1.4.7 | ![1.4 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.4.7%2Fmaven-metadata.xml) | - | - | - |
| 1.2.5 | ![1.2 Forge 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.7.10%2Fmaven-metadata.xml) | - | - | - |
| b1.7.3 | - | - | ![b1.7.3 babric 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-b1.7.3%2Fmaven-metadata.xml) | - |
| BTA | - | - | ![BTA babric 版本徽章](https://img.shields.io/maven-metadata/v?color=forestgreen&label=release&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-BTA%2Fmaven-metadata.xml) | - |

#### gradle.properties
```ini
# CPM 版本
cpm_api_version=<api 版本>
cpm_mc_version=<Minecraft 版本>
cpm_runtime_version=<运行版本>
```

#### 使用 FG2 的依赖项
```gradle
dependencies {
  compile "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  deobfProvided "com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}"
}
```

#### 使用 FG3+ 的依赖

```gradle
dependencies {
  /* Minecraft 依赖在这里 */

  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  runtimeOnly fg.deobf("com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}")
}
```

#### 使用 NeoGradle 的依赖

```gradle
dependencies {
  /* Minecraft 依赖在这里 */

  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  runtimeOnly "com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}"
}
```

#### 使用 Fabric 的依赖

```gradle
dependencies {
  /* Minecraft 依赖在这里 */
  
  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  modRuntimeOnly "com.tom5454.cpm:CustomPlayerModels-Fabric-${project.cpm_mc_version}:${project.cpm_runtime_version}"
}
```

#### 使用 Voldeloom 的依赖

```gradle
dependencies {
  /* Minecraft 依赖在这里 */
  
  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  //1.4.7, 1.5.2
  coremodImplementation ("com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}") {
    copyToFolder("coremods")
  }
  //1.6.4
  modRuntimeOnly "com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}"
}
```

On 1.2.5:  

```gradle
volde {
	runs {
		client {
			programArg "Dev"
			vmArg "-Dcpmcore.deobf=true"
			vmArg "-Dcpmcore.env.client=true"
			vmArg "-javaagent:\"" + file("CustomPlayerModels-${project.cpm_runtime_version}.jar").absolutePath + "\""
		}
	}
}

dependencies {
  /* Minecraft 依赖在这里 */
  
  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
}
```

您必须将当前模组版本从 Modrinth 放置到项目根文件夹中，否则 Java 代理将无法加载。

## 创建你的插件

创建一个实现 `ICPMPlugin` 的类。

```java
public class CPMCompat implements ICPMPlugin {
	public void initClient(IClientAPI api) {
		//初始化客户端
	}
	
	public void initCommon(ICommonAPI api) {
		//通用的初始化
	}
	
	public String getOwnerModId() {
		return "example_mod";
	}
}
```

### Forge 1.2.5
插件加载器未实现。

### Forge 1.12 及更低版本
发送包含您的插件类位置的 IMC 消息。
`FMLInterModComms.sendMessage("customplayermodels", "api", "com.example.mod.CPMCompat");`

### Forge 1.16 及更高版本
发送 IMC 消息。
 
```java
public MyMod() {
	...
	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
	...
}
	
private void enqueueIMC(final InterModEnqueueEvent event)  {
	InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) () -> new CPMCompat());
	...
}
```

### Fabric
在 fabric.mod.json 文件中的`entrypoints`字段添加`cpmapi`字段，如下所示：

```json
"entrypoints": {
    "cpmapi": [ "com.example.mod.CPMCompat" ]
}
```

### Bukkit、Spigot、Paper（0.4.1+）
使用服务管理器注册您的插件，您的插件的`initCommon`方法将使用`ICommonAPI`实例调用。

```java
RegisteredServiceProvider<CPMPluginRegistry> rsp = getServer().getServicesManager().getRegistration(CPMPluginRegistry.class);
if (rsp != null)
	rsp.getProvider().register(new CPMCompat());
else
	log.info("Customizable Player Models plugin not installed, compat disabled");
```

## 客户端 API

### 语音动画
注册语音级别支持。
`IClientAPI:registerVoice(Player.class, player -> voiceLevel);`  
[Player.class](#client-player-class)  

注册语音级别支持。（UUID变体）（0.6.0+）
`IClientAPI:registerVoice(playerUUID -> voiceLevel);`  

注册语音静音支持。（0.6.0+）
`IClientAPI:registerVoiceMute(Player.class, player -> voiceMuted);`  
[Player.class](#client-player-class)  

注册语音静音支持。（UUID变体）（0.6.0+）
`IClientAPI:registerVoiceMute(playerUUID -> voiceMuted);`  

### 渲染 API
创建一个玩家渲染器以在任何玩家实体上渲染 CPM 模型。
`PlayerRenderer<Model, ResourceLocation, RenderType, MultiBufferSource, GameProfile> renderer = IClientAPI.createPlayerRenderer(Model.class, ResourceLocation.class, RenderType.class, MultiBufferSource.class, GameProfile.class)`  
对于 1.12 及更低版本需要使用：
`RetroPlayerRenderer<Model, GameProfile> renderer = IClientAPI.createPlayerRenderer(Model.class, GameProfile.class);`  
[Model.class](#client-model-class)  
[ResourceLocation.class](#client-resourcelocation-class)  
[RenderType.class](#client-rendertype-class)  
[MultiBufferSource.class](#client-multibuffersource-class)  
[GameProfile.class](#client-gameprofile-class)  

#### 使用 CPM 模型渲染实体
1. 在渲染之前使用渲染器设置 GameProfile 或 LocalModel。
`setGameProfile(gameProfile)`：渲染玩家模型
`setLocalModel(localModel)`：渲染本地模型，[加载本地模型](#加载本地模型)
2. 设置基础模型，必须是人形或双足模型：`setRenderModel(model)`
3. 在 1.16+ 上设置默认 RenderType 工厂：例如：半透明实体：`setRenderType(RenderType::entityTranslucent)`。
4. 设置模型姿势，使用`getAnimationState()`、`setActivePose(pose)`、`setActiveGesture(gesture)`将动画应用到模型。
5. 调用 `preRender(buffers, mode)`（或 1.12 版中的 `preRender(mode)`）
6. 正常渲染您的模型。CPM 已将其渲染器注入您的模型。（使用 `getDefaultRenderType()` 获取模型的 RenderType（1.16+））。
    1. 要渲染其他模型（鞘翅、披风、盔甲），请调用：`prepareSubModel(model, type,texture)`（或 1.12- 上的 `prepareSubModel(model, type)`）
    2. 可选在 1.16+ 版本中更改模型的默认 RenderType：例如：`setRenderType(RenderType::armorCutoutNoCull)`
    3. 渲染你的模型（使用`getRenderTypeForSubModel（subModel）`获取模型的 RenderType（1.16+））。
7. 调用`postRender()`完成渲染。

示例（1.18 Forge）：

```java
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.IClientAPI.PlayerRenderer;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;

public class ExampleRenderer extends LivingEntityRenderer<ExampleEntity, PlayerModel<ExampleEntity>> {
	private static PlayerRenderer<Model, ResourceLocation, RenderType, MultiBufferSource, GameProfile> renderer;

	public static void init(IClientAPI api) {
		renderer = api.createPlayerRenderer(Model.class, ResourceLocation.class, RenderType.class, MultiBufferSource.class, GameProfile.class);
		
		// TODO: 替换为资源重载监听器以支持资源包。
		try (InputStream is = Minecraft.getInstance().getResourceManager().open(new ResourceLocation("example_mod", "models/example_entity_model.cpmmodel"))){
			renderer.setLocalModel(api.loadModel("example_entity_model", is));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ExampleRenderer(Context pContext, float pShadowRadius) {
		super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), pShadowRadius);

	}

	@Override
	public void render(ExampleEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		renderer.setRenderModel(model);
		renderer.setRenderType(RenderType::entityTranslucent);
		// 使用 renderer.getAnimationState()、setActivePose(name) 或 setActiveGesture(name) 设置模型姿势
		renderer.preRender(pBuffer, AnimationMode.PLAYER);
		if(renderer.getDefaultTexture() != null) {
			super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
		} else {
			renderNameTag(pEntity, pEntity.getDisplayName(), pMatrixStack, pBuffer, pPackedLight);
		}
		renderer.postRender();
	}

	@Override
	public ResourceLocation getTextureLocation(ExampleEntity pEntity) {
		return renderer.getDefaultTexture();
	}
}
```

当模型在后台加载时，`renderer.getDefaultTexture()`将返回 null！

#### 加载本地模型
从“.cpmmodel”文件加载模型。
`IClientAPI.loadModel(name, inputstream)`
使用加载的模型进行[渲染实体](#使用-cpm-模型渲染实体)

### 注册编辑器生成器
为编辑器注册一个模型生成器。生成器位于“编辑/工具”下。  
`IClientAPI.registerEditorGenerator("button.example_mod.example_generator", "tooltip.example_mod.example_generator", ExampleGenerator::apply);`  

```java
public class ExampleGenerator {
	public static void apply(EditorGui gui) {
		//TODO: 应用生成器
		// 使用 gui.getEditor() 访问编辑器
		// 使用 Editor.action 和 ActionBuilder 进行可撤消的更改。
		// 注意：编辑器的部分内容可能会发生变化。
	}
}
```

本地化（i18n）：
将 `button.example_mod.example_generator`、`tooltip.example_mod.example_generator` 添加到您的语言文件中。在工具提示中使用 `\` 字符作为换行符。

### 播放动画（0.6.0+）
为玩家播放指定的命令动画（客户端）。

name：动画名称
`IClientAPI.playAnimation(name);` 或
value：0：重置姿势/手势，1：播放姿势/手势，对于图层值：0-255，切换：0-1 或 -1 切换状态
`IClientAPI.playAnimation(name, value);`  
返回：如果找到动画并开始播放，则返回 true

### 检测动画 (0.6.14+)
检测玩家是否正在播放动画
`int value = IClientAPI.getAnimationPlaying(name);`  
返回：动画值（值层：0-255，其他动画：0-1），如果动画不存在则返回 -1

### 客户端网络（0.6.1+）
注册NBT消息发送到服务器，或广播给其他客户端。
`MessageSender sender = IClientAPI.registerPluginMessage(Player.class, message_id, (player, message) -> {/*Handle message*/}, broadcastToTracking);`  
[Player.class](#client-player-class)  
或 UUID 版本：
`MessageSender sender = IClientAPI.registerPluginMessage(message_id, (player_uuid, message) -> {/*Handle message*/}, broadcastToTracking);`  
使用`MessageSender`发送消息。
`boolean success = sender.sendMessage(message_tag);`  
使用`com.tom.cpl.nbt.*`包的独立于平台的 NBT 实现。
broadcastToTracking：false：将消息发送到服务器/true：向附近的玩家广播消息（通过服务器）。
服务器端需要 CPM 0.6.1+ 才能实现网络功能。
当使用broadcastToTracking或状态消息时，服务器上不需要您的模组/插件即可使数据包转发正常工作，您无需注册任何内容。 要接收非广播消息，请使用 ICommonAPI.registerPluginMessage

#### 状态消息
最后的状态消息存储在服务器上，并发送给进入跟踪范围（渲染距离）的每个客户端。
`MessageSender sender = IClientAPI.registerPluginStateMessage(Player.class, message_id, (player, message) -> {/*Handle message*/});`  
[Player.class](#client-player-class)  
或 UUID 版本：
`MessageSender sender = IClientAPI.registerPluginMessage(message_id, (player_uuid, message) -> {/*Handle message*/});`  

### 类映射
类取决于您的 Minecraft 版本和模组加载器。
#### 客户端 Player.class
Minecraft Forge 1.12 及更低版本：`EntityPlayer.class`
Minecraft Forge 1.16 和 Fabric：`PlayerEntity.class`
Minecraft Forge 1.17 及更高版本：来自 `net.minecraft.*` 的 `Player.class`

#### 客户端 Model.class
Minecraft Forge 1.16+ 和 Fabric：来自 `net.minecraft.client.*` 的 `Model.class`
Minecraft Forge 1.12 及更低版本：`ModelBase.class`

#### 客户端 ResourceLocation.class
Minecraft Forge: `ResourceLocation.class`  
Fabric: `Identifier.class`  

#### 客户端 RenderType.class
Minecraft Forge: `RenderType.class`  
Fabric: `RenderLayer.class`  

#### 客户端 MultiBufferSource.class
Minecraft Forge 1.16: `IRenderTypeBuffer.class`  
Minecraft Forge (1.17+): `MultiBufferSource.class`  
Fabric: `VertexConsumerProvider.class`  

#### 客户端 GameProfile.class
来自 AuthLib 的 GameProfile：`com.mojang.authlib.GameProfile`

## 通用 API

### 设置模型（0.4.1+）
设置玩家模型
`ICommonAPI.setPlayerModel(Player.class, playerObj, base64Model, forced, persistent);`  
或
`ICommonAPI.setPlayerModel(Player.class, playerObj, modelFile, forced);`  
使用 `ModelFile.load(file);` 或 `ModelFile.load(inputstream);` 创建 ModelFile
或
`ICommonAPI.resetPlayerModel(Player.class, playerObj);`  
清除服务器设置模型
[Player.class](#通用-player-class)  

### 跳跃（0.4.1+）
为玩家播放跳跃动画。
`ICommonAPI.playerJumped(Player.class, playerObj);`  
[Player.class](#通用-player-class)

### 播放动画（0.6.0+）
为玩家播放指定的命令动画（服务器端）。

name：动画名称
`ICommonAPI.playAnimation(Player.class, playerObj, name);` or  
`ICommonAPI.playAnimation(Player.class, playerObj, name, value);`  
Value：0：重置姿势/手势，1：播放姿势/手势，图层值：0-255，切换：0-1 或 -1 切换状态
[Player.class](#通用-player-class)

### 服务器网络（0.6.1+）
注册NBT消息，用于接收非广播消息/向客户端发送消息。
`MessageSender<Player> sender = ICommonAPI.registerPluginMessage(Player.class, message_id, (player, message) -> {/*Handle message*/});`  
[Player.class](#client-player-class)  
使用`MessageSender`发送消息。
`boolean success = sender.sendMessageTo(player, message_tag);`  
或广播给附近的玩家：
`sender.sendMessageToTracking(player, message_tag, sendToSelf);`  
使用来自“com.tom.cpl.nbt.*”包的独立于平台的 NBT 实现。
sendToSelf：向参数 1 中选定的玩家发送消息

### 检测动画（0.6.9+）
检测玩家是否正在播放动画
`int value = ICommonAPI.getAnimationPlaying(Player.class, playerObj, name);`  
返回：动画值（值层：0-255，其他动画：0-1），如果动画不存在则返回 -1
[Player.class](#通用-player-class)  

### 类映射
类别取决于你的 Minecraft 版本和模组加载器。
#### 通用 Player.class
Minecraft Forge 1.12 及更低版本：`EntityPlayer.class`
Minecraft Forge 1.16 和 Fabric：`PlayerEntity.class`
Minecraft Forge 1.17 及更高版本：来自 `net.minecraft.*` 的 `Player.class`
Bukkit: 来自“org.bukkit.entity”的“Player.class”。
