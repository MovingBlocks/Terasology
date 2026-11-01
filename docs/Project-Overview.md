This is a overview of all the involved sites, GitHub repositories and related projects that are involved with Terasology

Sites
---------

Our online presence covers:

* [Portal / forum](http://forum.terasology.org) - main site for announcements and discussion
* [Meta Server](http://meta.terasology.org) - shows a list of game servers, modules, and so on. Can be used via API and is used as such by the game and launcher.
* [Splash Site](http://terasology.org) - a small GitHub Page (hosted [here](https://github.com/MovingBlocks/movingblocks.github.com) to intro the game, play via applet, or even run the soundtrack (top left - Flash)
 * Note: Both applets and flash are aging as technologies and may not work in some browsers due to support and security. Goal is to eventually replace with Java Webstart and HTML5.
* Social networks: [Reddit](https://www.reddit.com/r/Terasology) | [Facebook](https://www.facebook.com/Terasology) | [Twitter](https://twitter.com/Terasology) | [G+](https://plus.google.com/b/103835217961917018533/103835217961917018533/posts)

Primary Repositories
---------

The central components of Terasology live under two GitHub Organizations. The ones needed to run the base game are listed below.

See [Codebase Structure](Codebase-Structure.md) for more details on each piece

* [MovingBlocks](https://github.com/MovingBlocks) - this organization primarily contains the engine itself plus facades. It also holds some library projects - more below
  * [Engine](https://github.com/MovingBlocks/Terasology): The beating heart of the game. Also contains the PC Facade (the standard application) and the Core Module, as they're required for the base game to run normally
* [Terasology](https://github.com/Terasology) - this organization is entirely meant for hosting content modules. These come in two flavors
  * Root repos: Modules that follow the full Contributor Guidelines and may be maintained to some degree by the official community
  * Fork repos: Modules hosted by modders elsewhere on GitHub that follow the Modder Guidelines and are eligible for inclusion in official distributions and the launcher

Libraries
---------

We've created some library projects while working on Terasology that are used in-game

* [TeraBullet](https://github.com/MovingBlocks/TeraBullet) - Offers some voxel-world integrations with [JBullet](http://jbullet.advel.cz)
* [TeraOVR](https://github.com/MovingBlocks/TeraOVR) - Wrapper for the [Oculus Rift](http://www.oculusvr.com) SDK
* [Jitter](https://github.com/openleap/jitter) - Utility framework for the [Leap Motion](https://www.leapmotion.com/) (hosted under the [OpenLeap](https://github.com/openleap) organization)

Other projects
---------

These are indirect parts of the project, such as our supporting site work and launcher

* [Launcher](https://github.com/MovingBlocks/TerasologyLauncher) - the best way to run the game. Allows easy auto-updating and managing different versions of the game
* [Applet](https://github.com/MovingBlocks/FacadeApplet) - the Facade running the applet version of the game you can [play in your browser](http://terasology.org/#play)
* [Splash Site](https://github.com/MovingBlocks/movingblocks.github.com) - our GitHub-hosted front-end site at http://terasology.org offering a few quick links and what not - just in case our primary site gets slammed or something :-)
* [Gooey](https://github.com/MovingBlocks/Gooey) - our handy little [Hubot](http://hubot.github.com/)-based IRC bot offering witty banter and useful functionality like auto-creating GitHub repos. When he feels like it, anyway!
* [TeraMisc](https://github.com/MovingBlocks/TeraMisc) - a repository for miscellaneous stuff that doesn't really fit  anywhere else. Like raw model files, assorted utility scripts, stuff for our [XenForo](http://xenforo.com) site ([forum/portal](http://forum.terasology.org))

External projects
---------

These are noteworthy external projects we use

* [LWJGL](http://lwjgl.org) - foundation for graphics, sound, and input
* [Gradle-Git](https://github.com/ajoberstar/gradle-git) - makes Gradle even more magical by adding Git tasks
* [Jenkins CI](http://jenkins-ci.org) - continuous integration tool, builds our stuff at https://jenkins.terasology.io
* [Artifactory](http://www.jfrog.com/home/v_artifactory_opensource_overview) - repository manager, holds our builds and assorted library files, at https://artifactory.terasology.io
* [XenForo](http://xenforo.com) - our portal/forum site at http://forum.terasology.org


