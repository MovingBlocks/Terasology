This is an article for developers who want to use and create their own console commands (for example for mods).

Adding new console commands to Terasology is easy. The steps below show what is needed for console commands.
Command implementation

Command structure
==============

Every command must be implemented as a public method of a ComponentSystem. E.g. the following command just displays "hello world" in the console.

```java
@RegisterSystem
public class MyCommands extends BaseComponentSystem {

    @Command( shortDescription = "Say hello", helpText = "Writes hello world to the console" )
    public String hello() {
        return "hello world";
    }
}
```

Looking at this simple examples we can see several important things:
* The command method must be located in a class implementing ``ComponentSystem``
* The command annotation ``@Command`` is necessary for every command
* One can specify a short description and a (probably) longer help text
* A returned string will be shown in the console.

Creating your own console commands
------------------

To specify a new console command, just an annotated public method is needed in a ComponentSystem. The annotation is
```java
@Command()
```
and marks the method as a command. The command will have the same name as the method,
e.g. if you name you method ``public void hello()`` the command ``hello`` will be available in the game.

Short Descriptions and Help Text
-----------------
Short descriptions and help texts can be added via the method annotation. To specify a short description, just add
```java
shortDescription = "text for short description"
```
to the annotation, e.g.
```java
@Command( shortDescription = "some command description" )
```

Probably longer help text can be specified via ``helpText`` in the annotaion. An example is given below:
```java
@Command( helpText = "A command without short description, but with a longer help text." )
```


Displaying text
------------------
Any value returned from the command (string or object) will be displayed in the in game console.

You can also write directly to the console via the Console class.

Parameters
-------------------
Of course it is possible to give parameters/arguments to your command when executed in the command line. These
arguments are specified as method arguments. It is highly recommended to prefix the method arguments by a parameter
annotation, that is used for the command line help.
```java
@Command( shortDescription = "Echo-cho-cho-o-o", helpText = "Echoes the input text." )
public String echo(@CommandParam( value = "Message" ) String message) {
    return message;
}
```
The method above will add an ``echo <string>`` command that simply echoes the input text. The command is proper annotated,
resulting in user friendly help messages and command description.

The supported types for command parameters are: float, int, String.

Commands and Multiplayer
-------------------

By default, commands run locally - on the client side.
A command can be marked as runOnServer, in which case it will be replicated to the server in a multiplayer game and executed there:

```java
@Command(runOnServer = true)
```

In such a case, the command method can have a final EntityRef parameter. This will be populated with the Client entity of the calling player.

```java
@Command(shortDescription = "Sends a message to all other players", runOnServer = true)
public void say(@CommandParam("message") String message, EntityRef speaker) {
    // ...
}
```