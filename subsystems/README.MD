# Subsystems

Subsystems provide and extend engine functionality that might not be needed all the time.

A subsystem can provide an API to be implemented by other subsystems (API subsystem). Such an API subsystem does not provide any functionality on its own.

Typical examples for subsystems are:
* platform integration (e.g., Discord)
* native libraries usage
* network activity

> :warning: **Subsystems should not extend or provide gameplay features!** Use Modules instead.
>
> It is planned to allow combinations of modules with subsystems for new functionality in the future.

### Comparation between Module and Subsystem functionality:

  |           | Module                                            | Subsystem
--------------|---------------------------------------------------|----------
Boot          | at game start                                     | at game launch 
Sandbox       | Yes                                               | No
Installing    | Yes, in-game download from server or repository   | No, with facade or engine only
Dependencies  | only another Modules                              | any, except Modules
Build Script  | engine-driven                                     | free-style
