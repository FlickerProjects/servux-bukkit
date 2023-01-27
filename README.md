Servux-bukkit
==============
Servux-bukkit is a server-side plugin that provides extra support/features for some client-side mods when playing on a server.

Servux-bukkit is only needed/useful on the dedicated server side in multiplayer as an addon of **PaperShelled**.

In version 0.1.x it only has one thing, which is sending structure bounding boxes for MiniHUD so that it can render those also in multiplayer.

Compiling
=========
* Clone the repository
* Open a command prompt/terminal to the repository directory
* run 'gradlew download'
* run 'gradlew generateMappedJar'
* run 'gradlew build'
* The built jar file will be in build/libs/