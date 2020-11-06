Subsystems
-

Subsystems provides and extends engine functionality.

Can be api subsystem.

Likes:
* Platform integration
* Native libraries usage
* Network activity

>(!) Subsystems not should extends/provides gameplay features itself, use Modules instead.
>
>(not yet) you can combine module with subsystem for new functionality 

### Comparation between Module and Subsystem functionality:

  |      | Module | Subsystem
---------|--------|----------
Boot     | at game start | at game launch 
Sandbox  | Yes    | No
Installing | Yes, in-game download from server or repository | No, with facade or engine only
Dependencies | only another Modules, or Subsystems(not yet, without downloading) | any, except Modules
BuildScript Managment | engine driven | free-style

 




