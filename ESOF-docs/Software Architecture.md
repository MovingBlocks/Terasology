#Software Architecture

### Introduction

The aim of this report is to describe some aspects regarding Terasology's software architecture, following the 4+1 view model.

Said model is split into four views, the logical view (accompanied by a package diagram), the implementation view (accompanied by a component diagram), the deployment view (accompanied by a homonymous diagram) and, last but not least, the process view (accompanied by an analysis diagram).

All these views are linked through the use case view (the +1), whose diagram was developed in the last report.

### Logical View 

The logical view mainly focuses on the functional requirements and what the system (in another words, Terasology as a whole) should provide to its users. With this in mind, the system is decomposed in several abstractions and depicted as packages, object and object classes. The following package diagram describes this decomposition:

![Package Diagram](UML Models/Package_Diagram.png)

#### Interpretation

Terasology can be divided in 12 packages, 4 of them being used by the engine package, which is the package responsible for the game cycle. The rendering package renders the scene according to the current game state, the network package allows players to connect to servers on the network, the persistence package provides means to save the game state and restore it on future use and finally, the audio package handles the game's audio.

During game execution, information flows between the packages. Again, most of the information flows to, or from the engine package. The player input information is sent to the engine package, which is then used to, for instance, send the appropriate information to the world package, modifying the game world. The game world information is then sent back to the engine package, being used in updating the game cycle. The configurations selected by the user are also sent during game execution, since they can be changed during runtime. For instance, some of these configurations involve changing some of the algorithms used in rendering the world, so this flow of information is mandatory. The entities in the game (like NPCs) are also constantly changing their attributes (position, for instance). Therefore, information flow is also necessary between this package and the engine.

The game logic (which depends on the physics package) deal with the game logic and are used by the world and entitySystem packages, for example, for calculating gravity effect on the world and entities' elements.

### Implementation View

The implementation view (also known as development view) focuses on decomposing software into components (program libraries, or subsystems) that are then developed by a small number of developers. These components are split into a hierarchy of layers, with the higher layers depending from the lower layers. The following component diagram depicts Terasology's layer hierarchy and dependencies:

[Inserir diagrama de componentes aqui]

[Inserir interpretação do diagrama de componentes aqui]

### Deployment View

The deployment view (also know as the physical view) takes into account more hardware-related requirements of the system, such as availability, reliabilty, performance and scalability. The development view is concerned not only with the computational resources (depicted as nodes) and the connections between them, but also with the manifestation of said computational resources, in the form of artifacts. The following deployment diagram shows these connections regarding Terasology:

[Inserir diagrama de desdobramento aqui]

[Inserir interpretação do diagrama de desdobramento aqui]

### Process View

The process view, acts as a linking bridge between de development and logical views. It takes into account requirements such as availability and performance, adressing issues such as the system's integrity and fault tolerance, and tries to fit the logical view's main abstractions into those requirements. The process view may be viewed as set of independently executing programs, each of them consisting in a group of tasks forming an executable unit (in other words, a process). The following analysis diagram depicts this in a sequential manner.

[Inserir diagrama de análise aqui]

[Inserir interpretação do diagrama de análise aqui]