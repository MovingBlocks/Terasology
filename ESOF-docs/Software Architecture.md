#Software Architecture

### Introduction

The aim of this report is to describe some aspects regarding Terasology's software architecture, following the 4+1 view model.

Said model is split into four views, the logical view (accompanied by a package diagram), the implementation view (accompanied by a component diagram), the deployment view (accompanied by a homonymous diagram) and, last but not least, the process view (accompanied by an analysis diagram).

All these views are linked through the use case view (the +1), whose diagram was developed in the last report.

### Logical View 

The logical view mainly focuses on the functional requirements and what the system (in another words, Terasology as a whole) should provide to its users. With this in mind, the system is decomposed in several abstractions and depicted as packages, object and object classes. The following package diagram describes this decomposition:

[Inserir diagrama de pacotes aqui]

[Inserir interpretação do diagrama de pacotes aqui]

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