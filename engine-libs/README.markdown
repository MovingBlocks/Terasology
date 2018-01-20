The engine-libs houses a set of internal libraries of the Terasology engine.
Goal of this separation is to exclude and encapsulate responsibilities from the monolithic engine.
For this reason quality checks like test-coverage are more restrictive in this subprojects.

Each engine-library has to fulfil the following characteristics:
1. __No dependency to engine__  
Reason: The engine should depend on engine-libs and not vice-versa. 
Circular dependencies are always a design smell.
2. __Minimize class visiblity__  
Reason: Logic can be decoupled with interfaces. 
A factory encapsulates the logic for object creation. 
Input for the factory may be other interfaces from third engine-libs.
Output should be interfaces.
_Exception: interfaces, abstract base classes, systems, events and components may be public._
3. __Test per feature, not per line__  
Reason: Tests per line lead to a high coupling of test and production code and make tests fragile to changes. 
Tests should represent the requirements which the lib has to satisfy. A high test coverage enforces a need for every line of production code.
No [gold plating](https://en.wikipedia.org/wiki/Gold_plating_(software_engineering)) ;)