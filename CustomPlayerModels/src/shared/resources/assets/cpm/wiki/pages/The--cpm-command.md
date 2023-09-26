# The `/cpm` command
### Subcommands:
* [setskin](#setskin)
* [safety](#safety)
* [kick](#kick)
* [scaling](#scaling)
* [animate](#animate)
* [effects](#effects)
* [detect](#detect)

## setskin
Usage `/cpm setskin [-f|-t|-r] <target> <base64 model>`  
Flags:
* -f: Force set model, player can't change their own model.
* -t: Temporary model, set model will be cleared when the player logs out.
* -r: Reset model, remove server set model

Target: the player to set the model to  
Base64 model: model exported with the Base64 option.

## safety
Subcommands:
* [recommend](#safety-recommend)
* [set](#safety-set)

### safety recommend
Usage: `/cpm safety recommend <enable>`  
Enable sending safety settings recommendations. The options can be set using the [set](#safety-set) command.

### safety set
Usage: `/cpm safety set <option> <value>`  
Set recommended safety settings

## kick
Controls the kick users without the mod feature.  
Subcommands:
* [enable](#kick-enable)
* [disable](#kick-disable)
* [message](#kick-message)

### kick enable
Usage: `/cpm kick enable <kick time>`
Kick users without the mod installed, after the kick time (in ticks).

### kick disable
Usage: `/cpm kick disable`
Disable the kick feature

### kick message
Usage: `/cpm kick message <message...>`
Set the kick message

## scaling
Usage: `/cpm scaling <scaling option>`  
Only has an effect after re-logging.  
Subcommands:
* [limit](#scaling-limit)
* [enabled](#scaling-enabled)

### scaling limit
Usage: `/cpm scaling <scaling option> limit [player] <max>` or `/cpm scaling <scaling option> limit [player] <min> <max>`  
Set the Pehkui scaling limit for `scaling option`. Optionally can be set per player using the player argument, leave empty for global setting.

### scaling enabled
Usage: `/cpm scaling <scaling option> enabled [player] <enable>`  
Enable Pehkui scaling for `scaling option`. Optionally can be set per player using the player argument, leave empty for global setting.

## animate
Usage: `/cpm animate <target> <animation name> [value (0-255)]`  
Play the animation for the target player(s).  
Value: 0: reset pose/gesture, 1: play pose/gesture, for layers value: 0-255, toggle: 0-1 or leave empty to switch state.

## effects
Usage: `/cpm effects <effect> <options...>`  
Available effects:  
- `invisible_glow <true/false>`: Enable rendering the glowing layers for invisible players. On by default. Only has an effect after re-logging.  

## detect
Usage: `/cpm detect <target> <animation name> [value (0-255)]`  
Detect if the given animation is playing  
If the `value` parameter is present then the command checks if the animation value is equal to the parameter  

