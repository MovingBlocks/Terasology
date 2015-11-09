#INDEX TODO


#Introduction

This report aims to explain Terasology's architecture according to [4 + 1 view model](http://www.sjaaklaan.com/?e=137) of software architecture, mentioned in the lectures. 

We begin to make an approach to Terasology's architecture as well as to the model noted above and subsequently an approach a little more specific in respect to each view with the help of illustrative diagrams.

##Terasology's architecture (melhorar um quitos)

In Terasology's wiki there is an [Architecture page] (https://github.com/MovingBlocks/Terasology/wiki/Architecture) where we can find references to [Block Architecture](https://github.com/MovingBlocks/Terasology/wiki/Block-architecture), [Entity System Architecture](https://github.com/MovingBlocks/Terasology/wiki/Entity-System-Architecture), [Events and Systems](https://github.com/MovingBlocks/Terasology/wiki/Events-and-Systems) as well as references to other modules's architecture.

As mentioned in the [Codebase Strucutre](https://github.com/MovingBlocks/Terasology/wiki/Codebase-Structure) :

>**Engine** The heart and soul of Terasology. All facades and modules depend on the engine.

The engine (as well in most of the projects) it's the most important module, that's why we will focus at it's achitecture and on how it's connected to the other modules without going into modules's low-level specifications.


# 4 + 1 Architectural View Model

4+1 is a view model designed by Philippe Kruchten and it's composed by the following views:
* Logical view
  * UML diagram: package diagrams.
* Implementation view
  * UML diagram: component diagrams.
* Deployment view
  * UML diagram: deployment diagrams.
* Process view
  * UML diagram: Activity diagrams.
* Use Case View (+1)
  * UML diagram: Use case diagrams.

In this report we will work on the first 4 views given that the last view(+1) it's the Use Case View which has already been discussed on the previous report.

<a name="Implementação"/>
## Implementation view

This view is also known as the **development view** and illustrates the software components and their dependencies from a programmer's perspective concerning to the software management. It uses the UML Component diagram to show how the software is decomposed (into software components) for development.

<a name="Component"/>
### Component Diagram

Component diagram describes how components are wired together to form larger components and or software systems. 

* A component represents a modular part of a system that encapsulates its contents and whose manifestation is replaceable within its environment.
* Components are wired together by using an assembly connector to connect the required interface of one component with the provided interface of another component. This illustrates the **service consumer - service provider** relationship between the two components.
* To promote intercangeability, components shouldn’t depend directly on other components but rather on interfaces (that are implemented by other components).


The following diagram represents our implementation view of the project:

![Component diagram](https://github.com/dimamo5/Terasology/blob/sergio/ESOF-DOCS/Software Architecture/images/component diagram v1.0.png)

<a name="Process"/>
# Process View

The process view includes a set of processes which execute independently and which, all together, form the executable program. This presents a sequential view of the execution of the program isolating the different processes that take part in the game. The sequence demonstrated represents the way the game works and how which process comunicates with the others and with the user. For this reason, this diagram is essential to clarify the process of execution of the game. The following diagram is a sketch of the real sequence diagram which is more complex and includes many other processes. The processes represented are more relevant to clarify the line of execution of the game on a higher level.

![Process View](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/sequenceDiagram.png)

The **game engine** represents the main process which initiates the game and calls the **context** and the **menu**. The **context** initializes the visual aspects of the game. It calls the rendering, network, physics processes and many others which are needed for the game to work. Furthermore, it calls the right modules once the game mode is chosen. The **menu** allows two options, either leave the game (and reach the **end** state) or choose the game mode. Following this, the *game mode* executes, receiving the correct modules from the context, and the state of gaming is reached. This state is identified by the **play** process which includes the user playing the game. Finally, the **end** process is called and the application ends.

<a name="Conclusao"/>
# Conclusion and critical analysis

To sum up, it is relevant to conclude that the game itself works in a efficient way although there is room for improvement. After exploring the architecture of Terasology, it is noticeable the impact of each piece in such a high level project. Actually, we have learned that it is far harder to organize such a project and if the planning is not done right then the project will never be functional.

In respect to the [Implementation view](#Implementação) it should be noted that the [component diagram](#Component) presented in that section it's our interpretation of the system decomposition into software components and it's subjective, therefore likely to have different views from other people analysis. Part of our interpretation was based on the analysis of the reverse engineering diagrams used to have a more accurate perspective of the project's architecture. 

When it comes to the process view, it is important to refer that the chosen implementation turns out to be very efficient. Actually, due to the fact that each process deals with a different section of the code the sequence of called functions works very well when it comes to communicate between processes. 



