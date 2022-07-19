## Exporting Models

There are ~~three~~ two ways to export your model:  
* [Stored in Skin](#stored-in-skin)  
* [Model file](#model-file)  
* [Base64 (for developers only, not intended for normal players)](#base64)  

### Stored in Skin
The model will be stored in the unused area of your minecraft skin.  
This mode will work on vanilla servers, and servers without the mod.  
But you can't change your model while in-game.  
[More info below](#skin)

### Model file
The model will be stored in a `.cpmmodel` file inside `<your minecraft directory>/player_models`.  
This mode requires the mod/plugin to be installed on the server, and won't work on vanilla servers.  
You can change your model while in-game, under the `Gestures Menu/Models`.  
[More info below](#exporting-as-local-model)

### Base64
Base64 models are for server owners, map makers.  
The models can be loaded [using commands](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command#setskin), or the [API](https://github.com/tom5454/CustomPlayerModels/wiki/API-documentation#set-model-041).  
This options is for developers only.  

## Skin
To use custom models in-game you have to export the models, using the editor, and upload the exported skin.

![Image: Export Skin Gui](https://github.com/tom5454/CustomPlayerModels/blob/master/screenshots/export_gui.png)

Click the '...' button to set the output file, then press Export. You can change the base skin with the 'Change Vanilla Skin' button. This is the skin that will show up if you don't have the mod installed, by default it loads your current skin.
Skin Layer settings are used [for creating custom animations](https://github.com/tom5454/CustomPlayerModels/wiki/Animations#custom-animations-encoding).

If the model is too big to fit in the unused space on the skin file, then you have to upload it to my paste site, or you can create a [GitHub Gist](https://gist.github.com/) with your model data and link it to your skin.

![Image: Export Overflow Gui](https://github.com/tom5454/CustomPlayerModels/blob/master/screenshots/export_overflow_popup.png)

Press OK to finish the export.

After exporting upload the exported skin file as your Minecraft skin, using your Minecraft Launcher, or the Minecraft website.
The model data is written into unused space on the skin so it won't show up if you don't have the mod installed.
If the launcher says unable to load skin then try restarting it.

The model will load for everybody who has the mod installed on multiplayer servers.

## Exporting as local model
You can export models as a local model. You can change these models in the Gestures Menu/Models when on servers with Customizable Player Models installed or in singleplayer, even while in-game.
You can export a local model in File/Export and press the 'Export: ???' button to 'Export: Model'.  
The name will be used for the file name. You can set an icon for your model.
Click Export.  
Apply your model in `Edit/Models`, or in-game `Gestures Menu/Models`.  
Select your model and click apply below the preview.  
Local models only work in singleplayer and in multiplayer servers with the mod (or plugin) installed.
