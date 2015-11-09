




#Deployment view

Deployment view is used to show the system hardware and how software components are distributed across the hardware nodes. 
However is more centered in hardware requisites, where the software components are deployed. 
A good deployment diagram controls many parameters such as performance, scalability, maintainability and portability of the hardware.
Deployment diagrams can be used to visualize hardware topology of a system, describe the hardware components used to deploy software components 
and describe runtime processing nodes. It basically consists of nodes that are nothing but physical hardwares used to deploy the application and 
the relations between them, represented as an artifact.
The following deployment diagram refers to the connections in Terasology:

![Deployment diagram](/images/DV.png)


##Interpretation:

As represented in the diagram Terasology needs a computer to be played and the Java Runtime Environment or the Java Development Kit (version 1.7 or higher). 
It also needs an operation system that can run Java7 or higher. 
After running Terasology in the computer, this device can connect to other devices through a dedicated server or a local server, that also need to execute Terasology.
The main difference between this two connections is that in order to use a local server, one of the players must be the host and the rest 
must be connected in the same network area wich is not needed using a dedicated server. The players can acess through different networks. 



