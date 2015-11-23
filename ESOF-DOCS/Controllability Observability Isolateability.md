**Controllability** is related to the degree to which the **Component Under Test** has a single, well defined responsibility. 
All software components are part of a module that can be connected to a lot of other modules. Controllability is defined as the effort it takes to provide 
a program with the needed inputs, in terms of values, operations and behaviors. As mention in the forum if we take a look into 
engine module, which is associated with a lot of other modules, controllability tends to be low because it is harder to 
provide the system with testes for all possible situations in different cases. However when testing an isolated component, controllability must be higher 
because the environment in which the test will run is going to be mcuh smaller and controlable. Its interaction is limited to engine itself or
components form other modules or even components external to Terasology. It all dependes on the situation. In embebed modules or modules that depend from 
others controllability tends to be low. 
Testing components from modules or even the deepest engine modules can lead to a a higher controlability rather than testing the engine itself. 


**Observability** is related to the degree to which it is possible to observe (intermediate and final) test results. 
In Terasology as they mention in the forum they use Jenking to generate test code automatically it is written in JUnit. 
Jenkins is executed every time there is a single engine build or pull requests submitted to GitHub in order to prevent messing with everything done so far.
[Here] (http://jenkins.terasology.org/job/Terasology/) we can find the JUnit test results and in the [statistics webpage] (http://jenkins.terasology.org/view/Statistics/)
 we can find not just the results but also metrics across a large amount of code, such as quantity and coverage. Althout the same metrics are available for all modules they're not always active or appropriate. 
Other modules just don't have any unit tests because the author hasn't added any.
 
 Beyond unit tests they have several other metrics - in order of appearance in Jenkins:
 
* Checkstyle: scans our code style / conventions to make sure the code is consistently using the same style everywhere
* FindBugs: scans for some common code drawbacks that can easily lead to bugs
* PMD: more of the same - style, common issues, etc
* Open Tasks: simply looks for TODO tags in the code indicating something needs to be done
* Static analysis: aggregate of the above (all them of added together)
* Code coverage: how many lines of code are actually exercised by the unit tests