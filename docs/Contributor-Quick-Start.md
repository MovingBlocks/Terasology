## Required Knowledge

* [Git Basics](https://git-scm.com/docs/gittutorial) - We use Git for version control, so you should be familiar with the concept of branches, fetching and committing changes, and pushing to a code repository.
* [GitHub Basics](https://docs.github.com/en/get-started/quickstart/hello-world) - We host our code on and collaborate via GitHub, so you should be familiar with repositories, pull requests, and [forks](https://docs.github.com/en/github/collaborating-with-pull-requests/working-with-forks/about-forks).
* [Bash Basics](https://towardsdatascience.com/basics-of-bash-for-beginners-92e53a4c117a)<sup id="a0">[0](#f0)</sup> - While you can avoid the command-line in most situations, in some situations you'll need to use it, for instance to fetch additional modules into or update your workspace. Especially, any `groovyw` or `gradlew` commands you find in our documentation need to be executed from the command-line.


## Required Tools

* Java 17 JDK (e.g. [OpenJDK 17 from Adoptium (Eclipse Foundation)](https://adoptium.net/en-GB/temurin/releases/?version=17&package=jdk&arch=x64&os=any))
* Git Client (e.g. [Git for Windows](https://gitforwindows.org/) or [GitKraken](https://www.gitkraken.com/))<sup id="a1">[1](#f1)</sup>
* IDE (e.g. [IntelliJ](https://www.jetbrains.com/idea/download)<sup id="a1">[2](#f2)</sup>)


## Set up your Terasology Development Workspace

1. Clone https://github.com/MovingBlocks/Terasology to a local workspace
1. Change into the new "Terasology" directory
1. Grab our module foundation ("iota" line-up)<sup id="a2">[3](#f3)</sup>
   * `groovyw module init iota`<sup id="a3">[4](#f4)</sup>
1. (Optional) Open your workspace directory into IntelliJ; choose "Import Gradle Project" if prompted.
   * _Note: Simply open the directory, don't pick "New Project". Our Gradle config will setup the project for you._
1. Compile and start Terasology from source for the first time
   * On the command-line, use `gradlew game`
   * In IntelliJ, run the "TerasologyPC" run configuration
1. Select the "CoreSampleGameplay" gameplay
1. Start the game


## Start a Custom Basic Gameplay

1. Grab additional modules and their dependencies
   * `groovyw module recurse <module>` will fetch all the modules used by that in source form
1. Compile and start Terasology (see #Dev-Setup)
1. Select "CoreSampleGameplay" and click on "Advanced Game Setup"
1. Double-click on the module you grabbed to activate it
1. Start the game


## Get Help when Facing Issues

* Check out our wiki pages on [Troubleshooting](Troubleshooting-Developer.md)
* Join our community on [Discord](https://discord.gg/terasology) and ask questions


## Start Contributing

* Look through the code, change small things and see how they affect your game
* Find some ["Good First Issues"](https://github.com/MovingBlocks/Terasology/labels/Good%20First%20Issue) to work on
* Code and test your changes locally in-game
* Push to your fork for engine changes, create module forks and push module changes there
* Make PRs and ask the community for review

Check out the general [contributing guidelines](https://github.com/MovingBlocks/Terasology/blob/develop/.github/CONTRIBUTING.md).


## Go deeper

You can read through the intro topics for more details on exactly how to set up and develop.

* [Resources for Learning Git & GitHub](https://help.github.com/articles/good-resources-for-learning-git-and-github)
  * [Recommended Git Tutorial](http://learngitbranching.js.org)
  * [Recommended GitHub Tutorial](https://help.github.com/categories/bootcamp)
* [Codebase Structure](Codebase-Structure.md)
* [Module Dependencies](Module-Dependencies.md) - explains how you can have module `X` depend on module `Y` even at a specific version level
* [Developing Modules](Developing-Modules.md) - shows you how to interact with and organize modules that hold most the content for the game
* Advanced Gradle and Groovy usage: take a look at `gradlew tasks` and `groovyw usage`
* [Using Locally Developed Libraries](Using-Locally-Developed-Libraries.md)


## Notes

<b id="f0">0</b> If you feel more comfortable on other shell types, e.g. fish, zsh, powershell, etc. that's fine. [↩](#a0)

<b id="f1">1</b> For Windows users, we highly recommend Git for Windows which comes with both, a GUI client and shell integration for Git, and in addition brings the Git Bash which allows Windows users a unix-style command-line experience. [↩](#a1)

<b id="f2">2</b> We recommend IntelliJ, because we have a series of customizations that prepare run configurations, Git integration, and so on for the project. You can use any IDE (or none at all) that you like, however you will not be able to benefit from these customizations. [↩](#a2)

<b id="f3">3</b> To get all modules that are part of our release, grab the "omega" line-up instead. Please note, that this line-up requires more memory and time for compilation and start-up. Check our `Index` repo to see all available [line-ups](https://github.com/Terasology/Index/tree/master/distros). [↩](#a3)

<b id="f4">4</b> Please note, that both `gradlew` and `groovyw` are scripts locally present in your workspace root directory. On *nix systems, you can execute them from within the root directory by prefixing them with `./`. [↩](#a4)
