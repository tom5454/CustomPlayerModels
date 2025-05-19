
<a name="the-cpm-command"/>

# The `/cpm` command

> [!NOTE]
> This is the server-side CPM command see the client-side `/cpmclient` command [here](https://github.com/tom5454/CustomPlayerModels/wiki/The--cpmclient-command)


<a name="subcommands"/>

### Subcommands:
* [setskin](#setskin)
* [safety](#safety)
* [kick](#kick)
* [scaling](#scaling)
* [animate](#animate)
* [effects](#effects)
* [detect](#detect)


<a name="setskin"/>

## setskin
Usage `/cpm setskin [-f|-t|-r] <target> <base64 model>`  
Flags:
* -f: Force set model, player can't change their own model.
* -t: Temporary model, set model will be cleared when the player logs out.
* -r: Reset model, remove server set model

Target: the player to set the model to  
Base64 model: model exported with the Base64 option.  
See: [Base64 exports](https://github.com/tom5454/CustomPlayerModels/wiki/Exporting#base64)


<a name="safety"/>

## safety
Subcommands:
* [recommend](#safety-recommend)
* [set](#safety-set)


<a name="safety-recommend"/>

### safety recommend
Usage: `/cpm safety recommend <enable>`  
Enable sending safety settings recommendations. The options can be set using the [set](#safety-set) command.


<a name="safety-set"/>

### safety set
Usage: `/cpm safety set <option> <value>`  
Set recommended safety settings


<a name="kick"/>

## kick
Controls the kick users without the mod feature.  
Subcommands:
* [enable](#kick-enable)
* [disable](#kick-disable)
* [message](#kick-message)


<a name="kick-enable"/>

### kick enable
Usage: `/cpm kick enable <kick time>`
Kick users without the mod installed, after the kick time (in ticks).


<a name="kick-disable"/>

### kick disable
Usage: `/cpm kick disable`
Disable the kick feature


<a name="kick-message"/>

### kick message
Usage: `/cpm kick message <message...>`
Set the kick message


<a name="scaling"/>

## scaling
Usage: `/cpm scaling <scaling option>`  
Subcommands:
* [limit](#scaling-limit)
* [enabled](#scaling-enabled)
* [method](#scaling-method)
* [reset](#scaling-reset)
* [debug](#scaling-debug)


<a name="scaling-limit"/>

### scaling limit
Usage: `/cpm scaling <scaling option> limit [player] <max>` or `/cpm scaling <scaling option> limit [player] <min> <max>`  
Set the scaling limit for `scaling option`. Optionally can be set per player using the player argument, leave empty for global setting.


<a name="scaling-enabled"/>

### scaling enabled
Usage: `/cpm scaling <scaling option> enabled [player] <enable>`  
Enable scaling for `scaling option`. Optionally can be set per player using the player argument, leave empty for global setting.


<a name="scaling-method"/>

### scaling method
Usage: `/cpm scaling <scaling option> method [player] <method>`  
Set the scaling method for `scaling option`.  
Supported options:
* `disable` method acts like if the scaling mod isn't installed.  
* `default` pick the server default scaler.  
* `attribute` use vanilla attributes
* `pehkui` use the [Pehkui](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling#pehkui) to scale (when installed)
* See the [scaling page](https://github.com/tom5454/CustomPlayerModels/wiki/Scaling) for other supported mods.


<a name="scaling-reset"/>

### scaling reset
Usage: `/cpm scaling reset <player>`  
Reset the scaling for the selected player.


<a name="scaling-debug"/>

### scaling debug
Usage: `/cpm scaling debug <player>`  
Prints all of the scaling settings for the player: requested scale `Rq`, actual scale `V`, limits `Lim`, scaling method `M`.  
Example output:
```
Scaling debug for <player>:

- 'Entity Scaling' M: attribute, V: 1.5 (Rq: 1.5), Lim: 0.01 - 3.0
- 'Eye Height' M: Not Supported (Rq: 1.0)
- 'Jump Height' M: attribute, V: 1.0 (Rq: 1.0), Lim: 0.01 - 10.0
- 'Health' M: Scaling limited to normal value (Rq: 1.0)
```


<a name="animate"/>

## animate
Usage: `/cpm animate <target> <animation name> [value (0-255)]`  
Play the animation for the target player(s).  
Value: 0: reset pose/gesture, 1: play pose/gesture, for layers value: 0-255, toggle: 0-1 or leave empty to switch state.


<a name="effects"/>

## effects
Usage: `/cpm effects <effect> <options...>`  
Available effects:  
- `invisible_glow <true/false>`: Enable rendering the glowing layers for invisible players. On by default. Only has an effect after re-logging.  


<a name="detect"/>

## detect
Usage: `/cpm detect <target> <animation name> [value (0-255)]`  
Detect if the given animation is playing  
If the `value` parameter is present then the command checks if the animation value is equal to the parameter  
