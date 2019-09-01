# Warp
Warp is an addon for [Fabric's](https://github.com/FabricMC) Gradle plugin, [Loom](https://github.com/FabricMC/fabric-loom). During the source generation stage of a Fabric project setup, Warp uninlines inlined constants in the generated sources.

Uninlining is achieved using [Unpick](https://github.com/Daomephsta/unpick), and a set of Unpick definitions. Warp is intended for use with the [Weft](https://github.com/WeaveMC/weft) definitions, but it is not dependent on them.