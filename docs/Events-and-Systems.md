# Events and Systems

## Summary

_Events are sent to exactly 1 entity. Systems can define methods, that get called when specific events get sent to entities with certain components._

See also various tutorial modules and examples, such as https://github.com/Terasology/TutorialAssetSystem/wiki/Add-A-Dice

## Processing events

To make a class a system you annotate it with the `@RegisterSystem` annotation. If a system implements certain engine interfaces like `UpdateSubscriberSystem`, then the methods of that interface will automatically be called by the engine. In addition to that, systems can declare methods that get called when certain events occurred at certain entities. 

Event processing methods must have the following method signature:
* They must have a `@ReceiveEvent` annotation
* They must be public
* The type of the first argument must implement `Event`
* The second argument must be of type `EntityRef`
* The rest of the arguments (if there are any) must implement `Component`

The signature determines when the method will be called
* The first argument controls for which type of event the method will be called.
* The `@ReceiveEvent` has an optional attribute called components which takes an array of component classes. The method will only be called if the entity has all those components.
* If there are optional component arguments, then the method will only be called if the event receiving entity has all of those components (like with the components attribute of `@ReceiveEvent`).
* The `@ReceiveEvent` annotation has also a priority attribute. It specifies the order in which this event will be processed by systems that have registered for the same event.
* The `@ReceiveEvent` annotation has also a `netFilter` attribute, which simply spoken specifies whether the event should be processed only on the server, only on the client, or on both. See Javadoc for `RegisterMode` for how it actually works. Usually this network mode gets set globally for the whole class via `@RegisterSystem` annotation. 

All parameters will be filled when the event occurs:
* The first argument will be filled with the event that occurred.
* The second argument will be filled with the entity at which the event occurred.
* The remaining arguments will be filled with the components of the entity, at which the event occurred. As the existence of the components is a requirement for the method to be called, all arguments will be filled out.

Example:
```java
@ReceiveEvent(components = {MyComponent.class, LocationComponent.class})
public void onMyComponentAdded(OnAddedComponent event, EntityRef entity, MyComponent myComponent) {
```
The example method gets called, when the `OnAddedComponent` event occurs at an entity, which has all of the following components: `MyComponent`, `LocationComponent`. The listing of `MyComponent` both in `@ReceiveEvent`and in the component arguments is redundant, but increased readability in the upper case.

**Note:** Some events like the `OnAddedComponent` event are implicitly linked to a component and will only be offered to methods that require those arguments. In the upper case the event fires only when `LocationComponent` got added while `MyComponent` was present or when `MyComponent` got added while `LocationComponent` was present. When another component gets added, while `MyComponent` and `LocationComponent` are present, the method won't be called.

The following core events are linked to to a component and require handling methods to require them:
* `OnAddedComponent`
* `OnActivatedComponent`
* `OnChangedComponent`
* `BeforeDeactivateComponent`
* `BeforeRemoveComponent`

All other core events and probably all module events aren't linked to a component. Please read the Javadoc of any event you make a system for.

## Defining events

An event is a class that implements the interface `Event`.

Its fields should be private and should be made accessible via public getters. The event should have no setters but a constructor that takes the values for all fields and sets them.

For events that are intend for network transfer, a protected default constructor should be provided.

See also the next chapter for annotations that are necessary for having an event be transferred over the network.

## Sending events

The recommended way of sending events is via the send method of entities (see `EntityRef#send`)
```java
entity.send(new MyEvent(myArg1, myArg2));
```

Per default, events aren't sent over the network.

Events annotated with `@ServerEvent`, are sent to the server. Only systems on the server will then be able to process it. Typically those events are requests to the server to confirm a gameplay related change. For that reason their name often ends with Request instead of Event.

Events annotated with `@BroadcastEvent` are sent by the server to all clients.
* TODO: What happens if a client tries to send this event?

Events annotated with `@OwnerEvent` are sent by the server to the client that owns the entity. Typically a client only owns its character and stuff related to it.

If a system on a client/server is responsible for processing an event, it can and should also be defined via a network filter which can be specified in the `@RegisterSystem` annotation of the service or within `@ReceiveEvent` annotation of the handling method.

## Consumable events

Normally an event is processed by the event handling methods in the order of their priority. Events that implement `ConsumableEvent` can, however, be consumed. Once an event is consumed its event handling stops and the remaining event handlers (with  lower priority) do not see the event.

This is for example useful to determine what happens with user input: When the player is in a mine cart the input movement events may be consumed by a high priority mine cart event handler before they reach the normal movement handlers. 

The sender of consumable events can check if their event got consumed. Some consumable events are sent as a test to figure out if there is a new system that objects with the action being taken. For example the event `BeforeItemPutInInventory` can be consumed by a new system, to prevent the placement of items in a slot that is reserved for certain other items.