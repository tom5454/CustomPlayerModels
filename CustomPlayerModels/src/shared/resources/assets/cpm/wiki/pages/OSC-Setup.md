Addon mod required: [CPM OSC compat on CurseForge](https://www.curseforge.com/minecraft/mc-mods/cpmoscc), [CPM OSC compat on Modrinth](https://modrinth.com/mod/cpmoscc)  


<a name="setting-up-osc-triggered-animations-with-cpm"/>

## Setting up OSC triggered animations with CPM
You have to configure OSC under `Edit/Tools/OSC Settings`.  
Use `Edit/Tools/OSC Animation Wizard` to create OSC triggered animations.  
The animations must be a `Command Activated Animation`.  
Use the `Value Layer` animation type to map OSC values to CPM animations.  
Toggle Layers trigger above 0.5 (mapped)  
Value mapping:  
CPM requires normalized values (0-1), set the Minimum and Maximum value using the Animation Wizard.  
`mapped_value = (osc_value - minimum_value) / (maximum_value - minimum_value)`  

CPM is compatible with OSC/VMC values.


<a name="scripting"/>

## Scripting
You can use [TouchOSC](https://hexler.net/touchosc#get) to run lua scripts that react to OSC commands. [TouchOSC scripting examples](https://hexler.net/touchosc/manual/script-examples)  
Python library for OSC: [python-osc](https://github.com/attwad/python-osc)  


<a name="other-applications"/>

## Other applications
Face tracking: [VSeeFace](https://www.vseeface.icu/)  
Most OSC sending applications will work that works with VRChat. [VRChat OSC Resources](https://docs.vrchat.com/docs/osc-resources)  
