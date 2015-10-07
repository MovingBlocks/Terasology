# Analysis of the software developing process of Terasology

## 1. Terasology

![Terasology](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/terasology.png?raw=true)

**Terasology** is a game that pays tribute to *Minecraft* keeping the aspect level and some of the game mods from the original. 
This open-source project was inicially called "Blockmania" and was developed 
with the main objective of studying the procedures involved in creating 3D terrain as well as the rendering techniques 
in Java using the game development library **[LWJGL](http://www.lwjgl.org/)** .
Afterwards, a team took responsability of the game itself changing the name to **Terasology**, which is currently in pre-alpha.

##2. Software developing process
Given the fact that **Terasology** has dozens of developers, in order to efficiently manage the project contributions the administers chose to use the [issue tracking](https://github.com/MovingBlocks/Terasology/issues) system and Github's pull request. Each contributor participates according to a very well refined methodology. Firstly, it is urgent to fork the principal project. Secondly, when the contributor sees fit
he can issue a pull request to the main repository. These pull requests are avaliated by the administers - they use the 
*[Jenkins](https://wiki.jenkins-ci.org/display/JENKINS/Meet+Jenkins)* tool in order to automate the testing procedure and verification of the issue.

The project does not have a pre defined model due to the elevated number of volunteers and the constant spikes of activity/inactivity of the contribuitors 
as one of the administers points out. The funder and head developer insists in adopting the BDD (Behaviour Driven-Development) model 
- indication given by the latter in the [Unit Testing](https://github.com/MovingBlocks/Terasology/wiki/Unit-Testing) guide for new contributors.
This model allows more flexibility and eases the process of aquiring new colaborators as well as it betters the communication between the varied teams 
working on the project such as the test team (beta-testers) and the developing team (implementation of the game's logic).

![Screenshot do website Terasology](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/site.png?raw=true)
