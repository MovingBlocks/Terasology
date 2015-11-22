**Controllability** is related to the degree to which the **Component Under Test** has a single, well defined responsibility. 
For some modules that are connected with a lot of other modules, such as engine, controllability is reduced because it is harder to test for some cases 
all possible situations. 
However when looking at an isolated component it most present a higher controllability since its interaction is limited to engine itself, components 
External to Teratology or components from other modules. Therefore, with a higher controllability in components from modules, executing tests in them 
is easier  than in components from engine itself AS other components interior to engine because they have lower interactions with other components.


**Observability** is related to the degree to which it is possible to observe (intermediate and final) test results.
In Terasology they use Jenking to test code automatically, it generates all the automated unit tests, written in JUnit. 
Jenkins is executed every time there is a single engine build or pull requests submitted to GitHub.
Here (ADD LINK!! - http://jenkins.terasology.org/view/Statistics/) we can find the JUnit test results and in the statistics webpage (Add link!!! - http://jenkins.terasology.org/view/Statistics/)
 we can find not just the results but also metrics across a large amount of code, such as quantity and coverage. 
  
 Beyond unit tests they have several other metrics - in order of appearance in Jenkins (LINK!!!!!!!!!!!):
 
- Checkstyle: scans our code style / conventions to make sure the code is consistently using the same style everywhere
- FindBugs: scans for some common code drawbacks that can easily lead to bugs
- PMD: more of the same - style, common issues, etc
- Open Tasks: simply looks for TODO tags in the code indicating something needs to be done
- Static analysis: aggregate of the above (all them of added together)
- Code coverage: how many lines of code are actually exercised by the unit tests

**Isolateability** is related to the degree to which the component under test (CUT) can be tested in isolation.
