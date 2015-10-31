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

## Implementation view

This view is also known as the **development view** and illustrates the software components and their dependencies from a programmer's perspective concerning to the software management. It uses the UML Component diagram to show how the software is decomposed (into software components) for development.

### Component Diagram

Component diagram describes how components are wired together to form larger components and or software systems. 

* A component represents a modular part of a system that encapsulates its contents and whose manifestation is replaceable within its environment.
* Components are wired together by using an assembly connector to connect the required interface of one component with the provided interface of another component. This illustrates the **service consumer - service provider** relationship between the two components.
* To promote intercangeability, components shouldn’t depend directly on other components but rather on interfaces (that are implemented by other components).


The following diagram represents our implementation view of the project:



