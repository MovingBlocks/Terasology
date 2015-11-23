**Controllability** is related to the degree to which the **Component Under Test** has a single, well defined responsibility. 
All software components are part of a module that can be connected to a lot of other modules. Controllability is defined as the effort it takes to provide 
a program with the needed inputs, in terms of values, operations and behaviors. As mention in the forum if we take a look into 
engine module, which is associated with a lot of others controllability is reduced because it is harder to 
provide the system with testes for all possible situations in different cases. However when testing an isolated component, controllability must be higher 
because the enviroment in which the test will run is going to be must smaller and controlable. Its interaction is limited to engine itself or
components form other modules or even components external to Terasology. It all dependes on the situation. In embebed modules or modules that depend from 
others controllability tends to be low. 
Testing components from modules or even the deepest engine modules can lead to a a higher controlability rader than testing the engine itself.