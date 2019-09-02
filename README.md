# Warp
Warp is an addon for [Fabric's](https://github.com/FabricMC) Gradle plugin, [Loom](https://github.com/FabricMC/fabric-loom). During the source generation stage of a Fabric project setup, Warp uninlines inlined constants in the generated sources.

Uninlining is achieved using [Unpick](https://github.com/Daomephsta/unpick), and a set of Unpick definitions. Warp is intended for use with the [Weft](https://github.com/WeaveMC/weft) definitions, but it is not dependent on them.

### Usage
#### Maven Repository
Warp is hosted on the same maven as Loom, so no repositories need to be added, 
your buildscript will already contain the correct repository.
#### Plugin ID
Warp's plugin id is `io.github.weavemc.warp`, check [the Warp maven](https://maven.fabricmc.net/io/github/weavemc/warp/) for the latest version.  
#### unpickDefinitions configuration
The Unpick definitions Warp should use are specified by the `unpickDefinitions` configuration. Warp expects this configuration to specify a single dependency.  

The suggested set of Unpick definitions is Weft.  
The maven group for Weft is `io.github.weavemc` and its artifact id is `weft`, check [the Weft maven](https://maven.fabricmc.net/io/github/weavemc/weft/) for the latest version. 
Note that the number before the + is the target Minecraft version.

To use other sets of Unpick definitions, see their documentation.

### Discord
If you need support with any WeaveMC project, you can find it [here](https://discordapp.com/invite/NSUCTCK).
