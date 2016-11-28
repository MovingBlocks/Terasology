# Terasology

[![Build Status](http://jenkins.terasology.org/job/Terasology/badge/icon)](http://jenkins.terasology.org/job/Terasology/)
[![Release](https://img.shields.io/github/release/MovingBlocks/Terasology.svg)](../../releases/latest)
[![Downloads](https://img.shields.io/github/downloads/MovingBlocks/Terasology/latest/total.svg "Downloads")](../../releases/latest)
[![Bounties](https://img.shields.io/bountysource/team/MovingBlocks/activity.svg)](https://www.bountysource.com/teams/MovingBlocks)
[![License(code)](https://img.shields.io/badge/license(code)-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![License(art)](https://img.shields.io/badge/license(art)-CC%20BY%204.0-blue.svg)](http://creativecommons.org/licenses/by/4.0/)
[![Dependency Status](https://www.versioneye.com/user/projects/537612b214c1584e82000022/badge.svg)](https://www.versioneye.com/user/projects/537612b214c1584e82000022)
[![IRC Channel](https://img.shields.io/badge/irc-%23terasology-blue.svg "IRC Channel")](https://webchat.freenode.net/?channels=terasology)

Welcome!

The Terasology project was born from a Minecraft-inspired tech demo and is becoming a stable platform for various types of gameplay settings in a voxel world.

The [creators and maintainers](https://github.com/MovingBlocks/Terasology/blob/develop/docs/Credits.md) are a diverse mix of software developers, designers, game testers, graphic artists, and musicians. We encourage others to join!

Terasology is fully [open source](https://github.com/MovingBlocks/Terasology) and licensed [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) for code and [CC BY 4.0](http://creativecommons.org/licenses/by/4.0/) for artwork (unless indicated otherwise - see [credits](docs/Credits.md) for minor exceptions)

We encourage contributions from anybody and try to keep a warm and friendly community and maintain a [code of conduct](https://github.com/MovingBlocks/Terasology/blob/develop/docs/Conduct.md)

![Terasology](/docs/images/menuBackground.jpg "Terasology")


## Playing

Terasology requires Java 8 - [download it here](https://www.java.com/en/download/). Also make sure that your graphics card driver is up to date.

For easy setup (recommended) you can use our launcher - [download it here](https://github.com/MovingBlocks/TerasologyLauncher/releases)

Direct download stable builds are uploaded to [our release section here on GitHub](https://github.com/MovingBlocks/Terasology/releases) while the cutting-edge develop version can be downloaded direct [here from our Jenkins](http://jenkins.terasology.org/job/DistroOmega/lastSuccessfulBuild/artifact/distros/omega/build/distributions/TerasologyOmega.zip)

For more information about playing like hot keys or server hosting see the [dedicated page](docs/Playing.md) or check out the [modules](docs/Modules.md)


## Developing

We have gone to great lengths to make developing and modding Terasology as easy as possible. We use Gradle to automate just about everything. As long as you have a [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) running from source is a two step process:

* Clone the code or download a zip
* Run `gradlew game` (on Unixes, including Mac OS X, run `./gradlew` everywhere you see `gradlew`) in the root of the project directory 

That's really it! If you want the project set up in IntelliJ (our favored IDE) you run `gradlew idea` then load the generated project config. Then you get a bunch of run configurations and other stuff for free!

For more on developing/modding see the [wiki](https://github.com/MovingBlocks/Terasology/wiki)


## Links

* [Game Credits](docs/Credits.md)
* [Code of Conduct](docs/Conduct.md)
* [Playing](docs/Playing.md)
* [Modules](docs/Modules.md)
* [Developing](https://github.com/MovingBlocks/Terasology/wiki)
* [Community Portal](http://forum.terasology.org)
* [Reddit](http://www.reddit.com/r/Terasology)
* [Twitter](https://twitter.com/Terasology)
* [Facebook](http://www.facebook.com/pages/Terasology/248329655219905)
* [G+](https://plus.google.com/b/103835217961917018533)
