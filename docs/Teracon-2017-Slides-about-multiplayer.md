Simplified in a multiplayer game you have:
* A server
* Multiple clients

However it is better to see it role wise:
* Authority
* Client:
  * Visual + Audio output
  * Masking of delay till server confirmed action
    
Example:
* Client: Handles left click by sending "attack request" to authority
* Authority: Authority determines outcome of "attack request" and informs clients

While we have only 2 roles, terasology has 4 modes it can run in:
* Headless / dedicated server:
  * Roles: Authority
* Remote client
  * Roles: Client
* Single player:
  * Roles: Authority and Client
* Listen server:
  * Roles: Authority and Client


How to let code only run in certain modes:
* Either for complete system class: `@RegisterSystem(RegisterMode.AUTHORITY)`
* Or per method: `@ReceiveEvent(netFilter = RegisterMode.CLIENT)`


Besides CLIENT and AUTHORITY there are 2 more modes:
* ALWAYS
  * The default
* REMOTE_CLIENT
  * For exotic optimizations

Typical bugs when multiplayer does not get kept in mind:
* Code for authority relies on something that got only created for the client
  * Code might fail only on "Headless server", and possibly on listen server when different client triggers action
* Code for client tries to do something that it has no permission for
  * Code fails only on remote clients
* Code that does not take network delays into account
  * Not responsive feeling on remote clients
  * Animations are not smooth 

This means:
* Testing with headless + remote client is necessary
* Headless + Modules => Painful to setup
* Multiplayer testing is time consuming

Long term vision:
* Singleplayer = listen server = headless server + remote client
  * Testing multiple modes would no longe be needed
  * Network delay could be simulated ingame for testing

This would mean:
  * Client sees no authority data
  * Authority sees no client data
  * Data exchange only via (simulated) network
 
In ideal world:
* There would be no static variables
* No CoreRegistry !

This would allow for:
* Separate `Context` instances
* authority related entities in authority entity manager
* UI related entites in client entity manager
* Constant data gets shared

Some ideas that take less effort:
* Option to start headless from UI
  * Instead of normal game
  * Possibly offers settings for bad network simulation
* Class loader tricks to allow for "headless server + remote client" in one JVM

Synchronization part:
 * See [Entities, Components and Events on the Network](Entities-Components-and-Events-on-the-Network.md)