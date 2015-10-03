<!---
	Describe project selected
-->

Project Description
--------

[Terasology](http://terasology.org/) is a project developed by the MovingBlocks github team. It was started by Benjamin "begla" Glatzel in order to research procedural terrain generation and efficient rendering techniques in Java using the LWJGL, a lightweight Java open-source library for game development.

In its essence, Terasology is a voxel based, Minecraft-like game, but tries to impose a NPC-helper gameplay like the games Dwarf Fortress and Dungeon Keeper.

Despite being already at its 4th anniversary of development, this project is still nearing alpha release, mostly due to unstable activity patterns, that is, periods of intense development followed by periods of inactivity as seen in [this](https://github.com/MovingBlocks/Terasology/graphs/contributors) graph. 


<!---
	Analyse development process used
-->

Development process
--------
Terasology is an open-source project that has had throughout its development dozens of contributors. Most of those contribute through pull requests (the others being the main developers). They rely on Github’s issue page to keep track of bugs and they also use [forums](http://forum.terasology.org/) and a [subreddit](https://www.reddit.com/r/terasology) to communicate between each other and with players.

Whenever a pull request is issued, the main developers of the project review and test it, providing feedback to the person that issued it. If it gets approved, it will be merged.

In order to test said pull request, they use Jenkins, a tool that builds the project and reports any compiler warnings and errors, as well as any JUnit test failures. The head developer has shown interest in automating [their Jenkins setup](http://jenkins.terasology.org/job/TerasologyPRs/), so that they don’t have to manually test them everytime they get a new pull request. 

According to the commit history, the project has been following a more incremental development and delivery approach. Although this has its advantadges, such as lower risk of failure or easier feedback from players/testers, this is not a good approach in our opinion. In a moderately sized project (like Terasology), this means that the structure of the project tends to degrade as time takes by, which means that a lot of time has to be invested in refactoring code in order to reestablish project structure. The head developer has expressed some concern regarding this situation, but the fact that the project is solely composed of volunteers and has had “roller coaster levels of activity” makes it difficult to implement a better software process. However, he has shown interest in implementing a more structured one, such as TTD or even BDD (Behaviour Driven Development) and has been trying to push in that direction.
