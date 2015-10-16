# Relatório 2 - ESOF
## Terasology - Gestão de Requisitos

###Requirements Elicitation

New requirements are suggested by everyone who wants to, through the [suggestions forum](http://forum.terasology.org/forum/suggestions.21/). From there, the original author of the suggestion (or someone else, provided they are interested in the suggestion), specifying what needs to be done in order to accomplish that requirement. After that, the topic is moved to the [modules forum](http://forum.terasology.org/forum/modules.55/), integrating it in a new module (or in one of the already existing modules). These modules are in independent repositories. When the code is implemented, tested, and working, it is merged into the repository of the module (without resorting to a pull request). A list of these modules (to which they refer as the Omega Distribution) can be seen [here](https://github.com/MovingBlocks/Terasology/blob/develop/README.markdown#modules).

In case the new requirement belongs to the game's core, the process is slightly different. The sugestor forks a project, and when he is finished coding the requirement, a pull requests is issued.

### Validation

As previously stated, the game development is split into several modules, each of them having an independent repository. Every few weeks, the main developer, [Cervator](https://github.com/Cervator), will go through the modules, tests them, and adds them to the main repository and, consequentely, to the next release. If it was added to the game's core (which is in the main repository), he will test and eventually accept the pull request.
Eventually, a new release will be made. Being an open-source project, with volunteers as contributors, it is difficult to get new releases in stable periods of time. However, they try to do it every 2-4 weeks. 
They use the [Semantic Versioning](http://semver.org/) norm to name their releases. It works as follows:

Given a version number MAJOR.MINOR.PATCH, increment the:

- MAJOR version when you make incompatible API changes,
- MINOR version when you add functionality in a backwards-compatible manner, and
- PATCH version when you make backwards-compatible bug fixes.
Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.