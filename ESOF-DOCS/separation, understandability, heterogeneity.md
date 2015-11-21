# Degree of testability

	**Terasology** is a very complex project which means that tests can only be applied to its subcomponents. This way, each module is divided in many other smaller modules. These are the ones that must be evaluated in order to better understand the degree of testability of the software.

## Separation of concerns

	**Terasology** is organized in several high level modules (the most complex) as it was refered in the previous report. These modules handle different tasks and, inside of them, the task is divided between various subunits. In our opinion, this separation is very efficient since it scatters the responsability through different units which can lead to a better performance and increases the degree of testability of each unit. Thus, it is concluded that the separation of concerns in **Terasology** is sucessful.

## Understandability

	The project has many ways to communicate with external contribuitors. Besides the [forum](http://forum.terasology.org/) and the [issues mechanism](https://github.com/MovingBlocks/Terasology/issues), the most important document to understand the code is the [wiki](https://github.com/MovingBlocks/Terasology/wiki) provided. The documentation of the code presented in the wiki is quite vast and complete which boosts the efficient communication with new contributors. If there are still doubts about certain pieces of the software, the leading developers are always available to discuss it. To sum up, it is safe to say that the program is very well documented and its degree of understandability is very high.

## Heterogeneity

	**Terasology** has a high level of heterogeneity as it uses a wide range of external technologies in order to run the game. The list of dependencies can be consulted in the file that permits the compilation of the code. The main used librarys are related to Java such as the LWJGL (refered in previous reports) and Jinput which add functionalities to the game. On the list there are some librarys developed specifically for the project such as **gestalt-module**, **gestalt-asset-core**, **TeraMath**, **tera-bullet** and **splash-screen**. Some of the used technologies are the following:

	1. **guava**, **netty**, **gson**
		* for purposes of networking and memory

	2. **java3d**, **lwjgl_util**, **miglayout-core**, **PNGDecoder**
		* for graphics and others

	3. **reflections**, **javassist**, **jna-platform**
		* for specific Java functionalities

	There are many other librarys and external technologies used for the game which are listed on the document mentioned above.

	The use of external technologies can be problematic due to the fact that it is not very clear whether the librarys are completely trustworthy or not. In **Terasology** the used librarys are well known so there should be no problems due to the use of those resources. To prevent possible failures it is important to develop tests to make sure that the test doesn't fail due to the external resources used. To conclude, the project has a high level of heterogeneity. Although, it is relevant to refer that the use of external dependencies should be managed with great care and caution. 
