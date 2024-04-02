## Java Agent Installation
The Minecraft 1.2.5 version of CPM is a java agent.  
1. Copy the CPM jar into your `.minecraft` folder.  
2. Then add the following JVM argument:  
`-javaagent:CustomPlayerModels-1.2.5-<version>.jar`
Replace the `<version>` with the version you installed.  

You have to update the JVM argument when updating CPM.

Minecraft Forge 1.2.5 is required

This version of CPM comes with a 'Single Player Commands' like mod included.  
Add the `-Dcpmcore.spc=true` JVM argument to enable.  
Supported commands:  
gamemode, tp, time, give, toggledownfall, cpm, cpmclient