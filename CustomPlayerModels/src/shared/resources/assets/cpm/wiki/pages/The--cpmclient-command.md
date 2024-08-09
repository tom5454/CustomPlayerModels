
<a name="the-cpmclient-command"/>

# The `/cpmclient` command
Added in 0.6.15

> [!NOTE]
> Forge versions between 1.14 to 1.17 and fabric versions below 1.19 don't support client-side commands
> This command is disabled on these versions.

> [!NOTE]
> This is the client-side CPM command see the server-side `/cpm` command [here](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpm-command)


<a name="subcommands"/>

### Subcommands:
* [profile](#profile)
* [safety](#safety)
* [animate](#animate)
* [set_model](#set_model)
* [reset_model](#reset_model)


<a name="profile"/>

## profile
Usage `/cpmclient profile <target>`  

Opens the social settings menu for the selected player  
Target: the player name


<a name="safety"/>

## safety
Opens the safety settings menu


<a name="animate"/>

## animate
Usage: `/cpmclient animate <animation name> [value (0-255)]`  
Play the animation for yourself.  
Only allows command controlled animations if it has the `client:` prefix.  
Value: 0: reset pose/gesture, 1: play pose/gesture, for layers value: 0-255, toggle: 0-1 or leave empty to switch state.


<a name="set_model"/>

### set_model
Usage: `/cpmclient set_model <model file>`  
Change your current model to the entered model file.  
Only works on server with the mod installed.  
This is identical to using the Models Menu.  


<a name="reset_model"/>

### reset_model
Remove your currently selected model, and allow loading from skin.  
This is identical to using the Models Menu/Load from Skin button.  
