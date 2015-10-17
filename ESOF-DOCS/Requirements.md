
#Requirements
## Index
1. [Introduction](#Introduction)
2. [Elicitation](#Elicitation)
3. [Validation](#Validation)
4. [Versioning](#Versioning)
5. [Use Cases](#Use Cases)


<a name="Introduction"/></a>
## Introduction

1. Purpose:

    The main purpose of this document is to give a detailed description about **Terasology**. 
    We intent to illustrate the purpose, features and goals of the game; what we can do with it. This document is directed to both the developers and users of Terasology.


2. Scope:

    Terasology was developed with the purpose of studying the procedures involved in creating 3D terrain as well as the rendering 
    techniques in Java using the game development library LWJGL. Terasology, which is currently in pre-alpha,
    is a game where the main key is building an estate of some sort and managing specialized minions to climb up the ladder of discovery, 
    while surviving in a world that might just be full of things that want to kill you.
 
3.  Definitions, acronyms and abbreviations:

    LWJGL -  is a Java library that enables cross-platform access to popular native APIs useful in the development of graphics, audio and parallel computing applications. It is an enabling technology and provides low-level access. It is not a framework and does not provide higher-level utilities than what the native libraries expose. LWJGL is an open source software and freely available with no charge.

4. References:

    http://forum.terasology.org/ - Terasologys  forum webpage.
    
    https://github.com/Terasology - Terasologys gitHub webpage.
    
    https://github.com/MovingBlocks/Terasology/wiki - Terasologys wiki.

<a name="Elicitation"/>
## Requirements elicitation

Since the development process is mentioned by one of the head developers as being *«Nothing formal»*, the requirement maintenance is achieved through the [issue tracker](https://github.com/MovingBlocks/Terasology/issues), where all the issues as well as necessary improvements are identified. Through the [suggestion forum](http://forum.terasology.org/forum/suggestions.21/) anyone can post an idea. There are also [developer forums](http://forum.terasology.org/forum/developer-portal.5/) where they discuss the suggestions about their implementation and maintenance.
To the project leader Cervator, *«the ideal setup»* to build up new requisits would be to perform the following steps:

1. Suggestioning:
    * Someone posts an idea in the suggestion forum mentioned above.
2. Suggestion's avaliation:
    * Users on the forum give feedback and if it's a good idea it will move to the next step.
3. Initial design
    * The OP(Original Poster) or someone starts designing and projecting tecnical questions associated with the implementation.
4. Coding
    * The idea starts getting implemented in code. According to project leader "Cervator" this is usually where the process begins, the steps 1,2,3 are often skipped.
5. Module up
    * Module 
        * If the concept can be integrated into a module it's moved to the [module forum](http://forum.terasology.org/forum/modules.55/).
    * Art
        * If the main goal is to make assets for the game instead of code itself, it is moved to the [art forum](http://forum.terasology.org/forum/art-media.25/).
    * Architecture
        * If the goal is to support the game engine or other support library-level functionality, the concept is moved to the [Core Projects forum](http://forum.terasology.org/forum/core-projects.54/).
6. "Release"
    * As soon as the feature is completely ready it is validated and released. The validation of this feature is explained on the validation section (<a name="index"/>[Validation](#validation)).

In relation to the method used:
>As for the why to our process: well, it gives what little structure to the process we can apply without getting in the way of people wanting to do work. (...)  As noted often it gets short-cut when somebody is excited about a feature and shows up with it out of nowhere.
Cervator - Project lead

As quoted above, the process used is the best-fit for this type of project because it allows everyone to give their contribute and to work at their pace and at the same time it's all organized and structured. On the other hand, it makes planning and estimating hard because the project depends on contributors, which all have in their own specific method of work.
The project leader mentions in the forum that he aims to get releases out every 2-4 weeks but as it's been said it's dependent on the contributors and on the stability of the modules up to realease.

<a name="Validation"/>
## Validation

*Terasology* is developed by small groups in which one of them forks the project and the group works on that specific fork. Every time a contributor wishes to merge code into the main repository an issue is created derived from a pull request. These issues are reviewed normally by the user [Cervator](https://github.com/Cervator) (ideally by at least more than one person other than the author) and either approved, in which case the code is merged into the main repository, or denied when the code has errors. The code is tested locally to make sure it works and does what is advertised. If it is accepted, the main programmers still discuss whether it is a valuable adition to the project and only in this case is the code merged ([example issue](https://github.com/MovingBlocks/Terasology/pull/1760)). When it is denied the errors are reported on the issue back to the author of the pull request and after everything is settled the issue is closed. Often pull requests are specific and inherent to its direct author. When this occurs, the team leader reviews it himself or tries to find someone more familiar with the code to test it.

<a name="Versioning"/>
##Versioning usage
As mentioned before, the project leader tries to get out a release every 2-4 weeks. In order to make it easier to deal with the intertwined small parts of the project as well as being able to tell the scope of a changed library based on it's version the leading team tries to use [Semantic Versioning](http://semver.org/).  

<a name="Use Cases"/>
##Use Cases

![Use Case 1](https://github.com/dimamo5/Terasology/blob/Diogo/ESOF-DOCS/Requirements/images/game.png)

![Use Case 2](https://github.com/dimamo5/Terasology/blob/Diogo/ESOF-DOCS/Requirements/images/options.png)

![Use Case 3](https://github.com/dimamo5/Terasology/blob/Diogo/ESOF-DOCS/Requirements/images/network.png)
