Welcome to the Terasology Wiki!

This is a wiki for those interested in contributing to the development of the project.

It doesn't cover game content or how to play the game much, but check out the docs on [Playing](https://github.com/MovingBlocks/Terasology/blob/develop/docs/Playing.md) (including hotkeys etc) and [Modules](https://github.com/MovingBlocks/Terasology/blob/develop/docs/Modules.md) for some of that.

Use the sidebar to arrive at the most central topics this wiki covers.

See [What is Terasology](What-is-Terasology.md) for some more background information.

## Joining the Community & Asking for Help

Our main place of communication is on our [Discord Server](https://discord.gg/terasology).
Make sure to check in, introduce yourself and what you're interested in with regards to Terasology :wave:
For any playing related issues, leave us a note in the `#play-terasology` channel.
Troubleshooting workspace setup, compile / test issues or other development related issues can be raised in the `#terasology` channel or by [opening an issue on this repo](https://github.com/MovingBlocks/Terasology/issues/new/choose).

Our [forum](https://forum.terasology.org/forum/) is currently mainly used to track progress of our GSoC student projects.
However, it has a lot of more or less actionable ideas for improvement and a bunch of history of our current gameplays floating around, so feel free to roam around a bit and get inspired :wink:

## Contributing

Interested in getting involved with working on the game? Make sure to check out the [Contributor Quick Start](Contributor-Quick-Start.md) for setting up your first workspace and starting the game from source. It also has useful links on how to start with your first contribution.

We also apply for GSOC - [Google Summer of Code](https://developers.google.com/open-source/gsoc) and [GCI](GCI.md) - [Google Code-In](https://codein.withgoogle.com/) every year. So if you're a student and it is that time of the year maybe check it out!

## Architecture

Terasology is build from many building bricks, that together turn into a game.

The _engine_ forms the core, and resides alongside the default _facade_ and _subsystems_ in ([MovingBlocks/Terasology](https://github.com/MovingBlocks/Terasology)).

This core is backed by several in-house _libraries_, such as([MovingBlocks/gestalt](https://github.com/MovingBlocks/gestalt)) providing the entity system and module management, or our own UI library ([MovingBlocks/TeraNUI](https://github.com/MovingBlocks/TeraNUI)).
The actual game content is added by _modules_ on top of that.

All Terasology modules reside in the [Terasology](https://github.com/Terasology) Github organization.

![Terasology - High Level Architecture](architecture.png)

These pages offer more advanced insight into how specific features of the game are architected and why.

- [Project Structure](Codebase-Structure.md) - a high-level overview of the code base
- [Entity System Architecture](Entity-System-Architecture.md) - describes the structure and usage of the entity system.
- [Events and Systems](Events-and-Systems.md) - describes how new game logic can be hooked in
- [Block Architecture](https://github.com/Terasology/TutorialAssetSystem/wiki/Block-Attributes) - development overview of our Block system. (pending changes needed to make the game work in an applet again)
- [Block Shapes](Block-Shapes.md) - defining 3D meshes via definitions in JSON!

## Announcement Channels

We have several ways to get the word out on updates, likewise, there are several ways to contact us.

- [Discord](https://discordapp.com/invite/terasology) - New development/game topics will be posted in `#announcement`, and any questions answered.
- [GitHub (Engine)](https://github.com/MovingBlocks/Terasology) - "Watch" the official project here to be able to easily spot core commits and changes.
- [GitHub (Modules)](https://github.com/Terasology) - "Watch" the module repos to be able to keep track of game content fixes / changes.
- [Forum](http://forum.terasology.org/) - Find the progress reports of ongoing and past GSoC projects along with a lot of gameplay ideas and lore
- [Twitter](http://twitter.com/#!/Terasology) - We'll tweet regularly about significant commits or new discussion topics posted, so "Follow" us for updates.
- [Facebook](http://www.facebook.com/pages/Terasology/248329655219905) - If you prefer to keep updated via Facebook you can "Like" us on there to keep up.
- [Jenkins RSS](http://jenkins.terasology.org/rssAll) - If you really want to know when something has just been built ;-)
