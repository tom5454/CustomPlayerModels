# CPM Api documentation
Table of contents:
* [Setup](#setup) ([Versions](#latest-versions))
* [Create your Plugin](#create-your-plugin)
* [Client API](#client-api)
* [Common API](#common-api)

## Setup
Add the CPM api to your gradle build script (`build.gradle`):  

### Manual
Download the api from [Releases](https://github.com/tom5454/CustomPlayerModels/releases/tag/0.4.0a)
and put it into your mod dev folder.  
Add it to your gradle file

```gradle
dependencies {
	implementation files("CustomPlayerModels-API-0.4.0.jar");
}
```

### Gradle

#### Repositories

```gradle
repositories {
  maven {
    name = "tom5454 maven"
    url = "https://raw.githubusercontent.com/tom5454/maven/main"
  }
}
```

#### Latest versions
API version: ![API version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-API%2Fmaven-metadata.xml)

| Minecraft Version | Runtime version (Forge) | Runtime version (Fabric) |
| ----------------- | ----------------------- | ------------------------ |
| 1.20 | ![1.20 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.20%2Fmaven-metadata.xml) | ![1.20 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.20%2Fmaven-metadata.xml) |
| 1.19.4 | ![1.19.4 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.19.4%2Fmaven-metadata.xml) | ![1.19 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.19.4%2Fmaven-metadata.xml) |
| 1.19.3 | ![1.19.3 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.19.3%2Fmaven-metadata.xml) | ![1.19 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.19.3%2Fmaven-metadata.xml) |
| 1.19 | ![1.19 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.19%2Fmaven-metadata.xml) | ![1.19 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.19%2Fmaven-metadata.xml) |
| 1.18 | ![1.18 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.18%2Fmaven-metadata.xml) | ![1.18 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.18%2Fmaven-metadata.xml) |
| 1.17 | ![1.17 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.17%2Fmaven-metadata.xml) | ![1.17 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.17%2Fmaven-metadata.xml) |
| 1.16 | ![1.16 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.16%2Fmaven-metadata.xml) | ![1.16 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.16%2Fmaven-metadata.xml) |
| 1.15 | ![1.15 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.15%2Fmaven-metadata.xml) | ![1.15 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.15%2Fmaven-metadata.xml) |
| 1.14 | ![1.14 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.14%2Fmaven-metadata.xml) | ![1.14 fabric version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-Fabric-1.14%2Fmaven-metadata.xml) |
| 1.12.2 | ![1.12 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.12.2%2Fmaven-metadata.xml) | - |
| 1.10.2 | ![1.10 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.10.2%2Fmaven-metadata.xml) | - |
| 1.8 | ![1.8 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.8%2Fmaven-metadata.xml) | - |
| 1.7.10 | ![1.7 forge version badge](https://img.shields.io/maven-metadata/v?color=forestgreen&label=beta&metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Ftom5454%2Fmaven%2Fmain%2Fcom%2Ftom5454%2Fcpm%2FCustomPlayerModels-1.7.10%2Fmaven-metadata.xml) | - |

#### gradle.properties
```ini
# CPM versions
cpm_api_version=<api version>
cpm_mc_version=<minecraft version>
cpm_runtime_version=<runtime version>
```

#### Dependencies using FG2
```gradle
dependencies {
  compile "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  deobfProvided "com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}"
}
```

#### Dependencies using FG3+

```gradle
dependencies {
  /* minecraft dependency is here */

  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  runtimeOnly fg.deobf("com.tom5454.cpm:CustomPlayerModels-${project.cpm_mc_version}:${project.cpm_runtime_version}")
}
```

#### Dependencies using Fabric

```gradle
dependencies {
  /* minecraft dependency is here */
  
  compileOnly "com.tom5454.cpm:CustomPlayerModels-API:${project.cpm_api_version}"
  modRuntimeOnly "com.tom5454.cpm:CustomPlayerModels-Fabric-${project.cpm_mc_version}:${project.cpm_runtime_version}"
}
```

## Create your Plugin

Create a class implementing `ICPMPlugin`.

```java
public class CPMCompat implements ICPMPlugin {
	public void initClient(IClientAPI api) {
		//Init client
	}
	
	public void initCommon(ICommonAPI api) {
		//Init common
	}
	
	public String getOwnerModId() {
		return "example_mod";
	}
}
```

### Forge 1.12 and lower
Send an IMC message with your plugin class location.  
`FMLInterModComms.sendMessage("customplayermodels", "api", "com.example.mod.CPMCompat");`

### Forge 1.16 and up
Send an IMC message. 
 
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
Register your plugin class as entry point in your fabric.mod.json as such:

```json
"entrypoints": {
    "cpmapi": [ "com.example.mod.CPMCompat" ]
}
```

### Bukkit, Spigot, Paper (0.4.1+)
Register your plugin using the service manager, your plugin's `initCommon` method will be called with the `ICommonAPI` instance.

```java
RegisteredServiceProvider<CPMPluginRegistry> rsp = getServer().getServicesManager().getRegistration(CPMPluginRegistry.class);
if (rsp != null)
	rsp.getProvider().register(new CPMCompat());
else
	log.info("Customizable Player Models plugin not installed, compat disabled");
```

## Client API

### Voice animation
Register a voice level supplier.  
`IClientAPI:registerVoice(Player.class, player -> voiceLevel);`  
[Player.class](#client-player-class)  

Register a voice level supplier. (UUID variant) (0.6.0+)  
`IClientAPI:registerVoice(playerUUID -> voiceLevel);`  

Register a voice muted supplier. (0.6.0+)  
`IClientAPI:registerVoiceMute(Player.class, player -> voiceMuted);`  
[Player.class](#client-player-class)  

Register a voice muted supplier. (UUID variant) (0.6.0+)  
`IClientAPI:registerVoiceMute(playerUUID -> voiceMuted);`  

### Rendering API
Create a player renderer to render CPM models on any Humanoid Entity.  
`PlayerRenderer<Model, ResourceLocation, RenderType, MultiBufferSource, GameProfile> renderer = IClientAPI.createPlayerRenderer(Model.class, ResourceLocation.class, RenderType.class, MultiBufferSource.class, GameProfile.class)`  
For 1.12 and lower use:  
`RetroPlayerRenderer<Model, GameProfile> renderer = IClientAPI.createPlayerRenderer(Model.class, GameProfile.class);`  
[Model.class](#client-model-class)  
[ResourceLocation.class](#client-resourcelocation-class)  
[RenderType.class](#client-rendertype-class)  
[MultiBufferSource.class](#client-multibuffersource-class)  
[GameProfile.class](#client-gameprofile-class)  

#### Rendering an Entity with CPM model
1. Using the renderer set the GameProfile or LocalModel before rendering.  
`setGameProfile(gameProfile)`: Render player model  
`setLocalModel(localModel)`: Render a local model, [Loading a local model](#loading-a-local-model)  
2. Set the base model, must be a Humanoid or Biped model: `setRenderModel(model)`
3. Set the default RenderType factory on 1.16+: e.g.: translucent entity: `setRenderType(RenderType::entityTranslucent)`.
4. Pose the model, apply animations to the model using `getAnimationState()`, `setActivePose(pose)`, `setActiveGesture(gesture)`.
5. Call `preRender(buffers, mode)` (or `preRender(mode)`, on 1.12-)
6. Render your model normally. CPM has injected it's renderer into your model. (Use `getDefaultRenderType()` for getting the RenderType for the model (1.16+)).
    1. To render additional parts (elytra, cape, armor) call: `prepareSubModel(model, type, texture)` (or `prepareSubModel(model, type)` on 1.12-)  
    2. Optionally on 1.16+ change the default RenderType for the part: e.g.: `setRenderType(RenderType::armorCutoutNoCull)`
    3. Render your part (Use `getRenderTypeForSubModel(subModel)` for getting the RenderType for the model (1.16+)).
7. Call `postRender()` to finish rendering.  

Example (1.18 Forge):

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
		
		// TODO: replace with resource reload listeners to support resourcepacks.
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
		//Pose model using renderer.getAnimationState(), setActivePose(name) or setActiveGesture(name)
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

While the model is loading in the background `renderer.getDefaultTexture()` will return null!

#### Loading a local model
Load a model from a `.cpmmodel` file.  
`IClientAPI.loadModel(name, inputstream)`  
Use the loaded model for [Rendering an Entity](#rendering-an-entity-with-cpm-model)

### Register Editor Generator
Register a model generator for the editor. Generators are under `Edit/Tools`.  
`IClientAPI.registerEditorGenerator("button.example_mod.example_generator", "tooltip.example_mod.example_generator", ExampleGenerator::apply);`  

```java
public class ExampleGenerator {
	public static void apply(EditorGui gui) {
		//TODO: apply the generator
		// Use gui.getEditor() to access the editor
		// Use Editor.action and ActionBuilder to make undoable changes.
		// Note: parts of the editor may change.
	}
}
```

Localization:  
Add `button.example_mod.example_generator`, `tooltip.example_mod.example_generator` to your language file. Use `\` characters for line breaks in the tooltip.

### Play Animation (0.6.0+)
Play the given command animation for a player (Client-side).  

Name: Animation name  
`IClientAPI.playAnimation(name);` or  
Value: 0: reset pose/gesture, 1: play pose/gesture, for layers value: 0-255, toggle: 0-1 or -1 to switch state
`IClientAPI.playAnimation(name, value);`  
Returns: true if the animation was found and started playing  

### Client Networking (0.6.1+)
Register a NBT message to send to the server, or broadcast it to other clients.  
`MessageSender sender = IClientAPI.registerPluginMessage(Player.class, message_id, (player, message) -> {/*Handle message*/}, broadcastToTracking);`  
[Player.class](#client-player-class)  
or UUID version:  
`MessageSender sender = IClientAPI.registerPluginMessage(message_id, (player_uuid, message) -> {/*Handle message*/}, broadcastToTracking);`  
Use the `MessageSender` to send messages.  
`boolean success = sender.sendMessage(message_tag);`  
Use the platform independent NBT implementation from `com.tom.cpl.nbt.*` package.  
broadcastToTracking: false: Send the message to the server / true: broadcast message to nearby players (through the server).  
CPM 0.6.1+ is required on the server side for the networking to work.  
When using broadcastToTracking, or State Messages your mod/plugin is not required on the server for the packet forwarding to work, you don't have to register anything. To receive non broadcast messages use the ICommonAPI.registerPluginMessage  

#### State Messages
The last state message is stored on the server and sent to every client that enters the tracking range (render distance).  
`MessageSender sender = IClientAPI.registerPluginStateMessage(Player.class, message_id, (player, message) -> {/*Handle message*/});`  
[Player.class](#client-player-class)  
or UUID version:  
`MessageSender sender = IClientAPI.registerPluginMessage(message_id, (player_uuid, message) -> {/*Handle message*/});`  

### Class Map
Classes are dependent on your minecraft version and mod loader.  
#### Client Player.class
Minecraft Forge 1.12 and lower: `EntityPlayer.class`  
Minecraft Forge 1.16 and Fabric: `PlayerEntity.class`  
Minecraft Forge 1.17 and up: `Player.class` from `net.minecraft.*`  

#### Client Model.class
Minecraft Forge 1.16+ and Fabric: `Model.class` from `net.minecraft.client.*`  
Minecraft Forge 1.12 and lower: `ModelBase.class`  

#### Client ResourceLocation.class
Minecraft Forge: `ResourceLocation.class`  
Fabric: `Identifier.class`  

#### Client RenderType.class
Minecraft Forge: `RenderType.class`  
Fabric: `RenderLayer.class`  

#### Client MultiBufferSource.class
Minecraft Forge 1.16: `IRenderTypeBuffer.class`  
Minecraft Forge (1.17+): `MultiBufferSource.class`  
Fabric: `VertexConsumerProvider.class`  

#### Client GameProfile.class
GameProfile from AuthLib: `com.mojang.authlib.GameProfile`  

## Common API

### Set Model (0.4.1+)
Set the player model  
`ICommonAPI.setPlayerModel(Player.class, playerObj, base64Model, forced, persistent);`  
or  
`ICommonAPI.setPlayerModel(Player.class, playerObj, modelFile, forced);`  
Create a ModelFile using `ModelFile.load(file);` or `ModelFile.load(inputstream);`  
or  
`ICommonAPI.resetPlayerModel(Player.class, playerObj);`  
clear the server set model  
[Player.class](#common-player-class)  

### Jump (0.4.1+)
Play the jump animation on for a player.  
`ICommonAPI.playerJumped(Player.class, playerObj);`  
[Player.class](#common-player-class)

### Play Animation (0.6.0+)
Play the given command animation for a player (Server-side).  

Name: Animation name  
`ICommonAPI.playAnimation(Player.class, playerObj, name);` or  
`ICommonAPI.playAnimation(Player.class, playerObj, name, value);`  
Value: 0: reset pose/gesture, 1: play pose/gesture, for layers value: 0-255, toggle: 0-1 or -1 to switch state  
[Player.class](#common-player-class)

### Server Networking (0.6.1+)
Register a NBT message to receive non broadcast messages/send messages to clients.  
`MessageSender<Player> sender = ICommonAPI.registerPluginMessage(Player.class, message_id, (player, message) -> {/*Handle message*/});`  
[Player.class](#client-player-class)  
Use the `MessageSender` to send messages.  
`boolean success = sender.sendMessageTo(player, message_tag);`  
or broadcast to nearby players:  
`sender.sendMessageToTracking(player, message_tag, sendToSelf);`  
Use the platform independent NBT implementation from `com.tom.cpl.nbt.*` package.  
sendToSelf: Send message to the selected player in argument 1

### Detect Animation (0.6.9+)
Detect if an animation is playing for the player  
`int value = ICommonAPI.getAnimationPlaying(Player.class, playerObj, name);`  
Returns: The animation value (value layer: 0-255, other animations: 0-1), -1 if animation doesn't exist
[Player.class](#common-player-class)  

### Class Map
Classes are dependent on your minecraft version and mod loader.  
#### Common Player.class
Minecraft Forge 1.12 and lower: `EntityPlayer.class`  
Minecraft Forge 1.16 and Fabric: `PlayerEntity.class`  
Minecraft Forge 1.17 and up: `Player.class` from `net.minecraft.*`  
Bukkit: `Player.class` from `org.bukkit.entity`.

