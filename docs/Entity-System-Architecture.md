The **Entity Component System Architecture** (ECS) is built upon three pillars: entities, components, and systems.
We are going to give a brief overview of what those are, and how they work together in a game (engine) like Terasology.

- [Entities](#entities)
  - [EntityRef](#entityref)
  - [Prefabs](#prefabs)
- [Components](#components)
  - [Component Data Types](#component-data-types)
  - [Component Library](#component-library)
  - [Modifying Components](#modifying-components)
- [Systems](#systems)
- [Events](#events)
  - [Events in Detail](#events-in-detail)
- [Example modules](#example-modules)
- [Further Reading](#further-reading)

## Entities

At the core of the Entity System we have entities - these are identifiable "things" in the world.
However an Entity by itself doesn't do anything - it has no data, nor behavior.
It is merely a logical container for one or more [Components](#components) - these provide data, and behavior through their interaction with [Systems](#systems).

For instance, the player is an entity, a single enemy is an entity, a chest is also an entity.
Entities don't have to be objects that are visible in the world either - they can be scoreboards, loot tables, a UI, and other gameplay related things.

### EntityRef

In Terasology, entities are worked with through the _EntityRef_ class.
For the most part this just delegates requests to the _Entity Manager_ - the central manager for the Entity System.
The EntityRef itself does not store components, these are stored in the Entity Manager within a hashed table, to allow ease of iterating over all entities with a given type of component.

EntityRef uses the [Null Object Pattern](https://en.wikipedia.org/wiki/Null_object_pattern) - instead of using Java's null, `EntityRef.NULL` should be used.
`EntityRef.NULL` can be safely accessed with all its functions providing sensible default results - this removes the need to check whether an EntityRef is null in many cases.
Where it is needed, the `exists()` method can be called to check whether an EntityRef is a valid reference.

One particular feature of the EntityRef is that when an entity is deleted, all references to it are invalidated and act like `EntityRef.NULL` from then on.
This allows EntityRef to be safely used in components, and Entity ids to be reused.

### Prefabs

> 🚧 TODO: move this to sub-page or tutorial module

A prefab is a recipe for creating entities (a "pre-fabricated entity").
Prefabs mimic an actual entity - they have components with all the usual values - but are not processes by systems.
Instead, they can be used to create an entity with the same components and the same settings.

In Terasology, prefabs can be defined in a JSON format and included in modules.
A prefab file is a set of components with their respective parameters and values described in a JSON structure.

```json
{
  "name": "core:gelatinousCube",
  "Location": {},
  "Mesh": {
    "renderType": "GelatinousCube"
  },
  "CharacterMovement": {
    "faceMovementDirection": true
  },
  "SimpleAI": {},
  "AABBCollision": {
    "extents": [0.5, 0.5, 0.5]
  },
  "CharacterSound": {
    "footstepSounds": [
      "engine:Slime1",
      "engine:Slime2",
      "engine:Slime3",
      "engine:Slime4",
      "engine:Slime5"
    ],
    "footstepVolume": 0.7
  }
}
```

The prefab has a name that identifies it (this name will be derived from its file name in a module), and a number of components (referred to by the name of the component class with "Component" dropped from the end if present).
Within each component the default values for each property can be overridden.

Additionally, prefabs can be used as a read-only source of information, without ever making entities from them.
They could be used to describe crafting recipes for instance.

Prefabs can also be inherited within the same module, or from other modules.
This will inherit all of the parent components and settings and then add any additional components or change any different settings.

```json
{
  "name": "core:angryGelatinousCube",
  "parent": "core:gelatinousCube",
  "CharacterMovement": {
    "speedMultiplier": 0.3
  }
}
```

A few thumb rules for prefab inheritance are:

- When a prefab inherits another, all of the parent components are inherited as well. It is enough to create a `.prefab` file containing only the tag `{ "parent": "<parentPrefabName>"}`
- Duplicated components are overwritten: if the parent contains the same component as the child but with different values, the child's component description will prevail/overwrite the original. Similarly, components cannot be removed in the child's prefab: if a component is described in the parent prefab but not in the child, the entity created from the child prefab will contain the missing module as described in the parent prefab.
- Components can only be described and inherited in full. If a component described in the parent prefab possess more than one parameter but it is only necessary to change one of the values, all other parameters and values must be repeated in the child prefab.

Prefabs can then be instantiated at runtime through the EntityManager.

Usage of prefabs provides a number of benefits.
Prefabs reduce the size of save data and network data - rather than using the full set of data for an entity, just the prefab the entity uses and delta of differences can be used.
Prefabs also allow existing objects to be updated - when you change a prefab and load a level, any changes to the prefab will be reflected in any instances in that level.
There is also potential to use prefabs as static data - for instance they could be used to describe crafting recipes or materials without ever creating an entity.

Prefabs are still an area for future development - at the moment they have a number of limitations:

- You cannot specify nested entities that should be created and related to a EntityRef property when a prefab is instantiated (like the items in a player's starting inventory).
- A child prefab cannot remove an entity defined by a parent prefab.

## Components

A component is a meaningful set of data, with a intention of being used to provide behavior, that can be attached to an [Entity](#entities).
For instance, a Location component stores data relating to an entity's position in the world - with the implication that the entity is something in the world.
A Mesh component holds information about a mesh used to render the entity - but its presence along with a LocationComponent also implies the entity should be rendered.

Typically a component is a plain java object with a flat set of value objects and minimal functionality.
There may be some data related methods (to ensure values are not set to null, for instance), but there should not be any game logic - that should be provided by the [Systems](#systems).
Components may contain EntityRefs, but should never have a reference to other Components, or to Systems.

Components should be declared final.

> Each entity can have at most one Component of a given type - an entity may have a Location, but cannot have multiple locations.
> Generally if you come across a situation where having multiple of the same component may seem attractive, you would be better served by using multiple entities with a component to tie them to a "main" entity.

Components may only contain specific data types - this is used to support persistence.

This structure provides flexibility and reuse when creating entities - you can add any of the existing components to make use of their features, and concentrate on creating new components for any features that don't presently exist - if any.

### Component Data Types

- Standard Java Primitives - double / float / int / boolean / long and their equivalent boxed types
- String
- Enums
- Map&lt;String,X&gt;, where X is one of the supported types. The generic must be specified.
- List&lt;X&gt;, where X is one of the supported types. The generic must be specified.
- Set&lt;X&gt;, where X is one of the supported types. The generic must be specified.
- EntityRef
- BlockFamily
- Color4f
- Vector2f / Vector3f / Vector2i
- Quat4f
- Texture
- and simple POJOs composed of above (marked with the @MappedContainer annotation)

This list can be extended - see [Component Library](#component-library) section below.

### Component Library

The ComponentLibrary stores information about the components, which is used to handle persistence. When a component is registered with the ComponentLibrary, all of the properties of the component are enumerated, and a ComponentMetadata object is created with a list of those properties, their types, and methods to get and set that field in an object.

### Modifying Components

In general usage you use EntityRef's `addComponent` method to attach a component to an entity, and `removeComponent` to remove it.
`getComponent` can be used to retrieve a component for inspection or modification.
At the moment, after modifying a component `saveComponent` should be used to save it back to the EntityRef - this causes an event to be sent.
Future implementations may require this call for the component changes to actually occur though.

There are additional methods following a functional programming approach to update and "upsert" an (existing) component via `updateComponent` or `upsertComponent`, respectively.

## Systems

Systems provide behavior to entities. They do this in two ways

- Processing entities with a desired set of components in response to engine method calls like `initialise()`, `update(float delta)` and `render()`
- Responding to entity events sent to entities with a desired set of components

For example, a particle system would iterate over all entities with both a Location and a Particle component (need the location to give the effect a position in the world, and the particle component is needed for the entity to be a particle effect in the first place) in each `update(float delta)` call, to update the particle positions.
The health system would respond to an entity with a Health component receiving a Damage event, in order to reduce an entity's health - and if the health reaches 0, send a Death event to the entity.

A System provides the actual logic behind a component or combination of components - it can process them each frame, or respond to events dealing with them. Multiple components being involved is common - rendering a mesh from a Mesh component cannot be done without a location from a Location component, for instance.

There are a number of advantages to the separation between data (in the Components) and logic (in the Systems)

- **Allows modules to modify the behaviour of components.**
  If a modder doesn't like the default behaviour of the Health component, they can replace the Health system with their own version - that will still work with all the existing entities with a Health component.
- **Allows optimization of the processing of multiple entities.**

This triple system of [Entities](#entities) composed of [Components](#components) with [Systems](#systems) applying behavior provides a great deal of flexibility and extendability.
Often new behavior can be introduced through new components and systems.
Behavior can be completely changed by removing a system and putting a different one in its place.
And new entities can be created by mix and matching existing components and changing their settings, without even writing code.

## Events

Terasology also includes an event system built around the entity system.
Events are a mechanism used to allow systems to interact with each other.
Events are typed, and carry data - for instance, there is a DamageEvent that holds an amount of damage being caused, and the instigator responsible for the damage.
Events are sent to entities.
Systems can then provide event handlers to pick up specific events that have been sent entities with a desired set of components.

For instance, when an entity is hit by a sword a DamageEvent is sent to it.
This event includes the amount of damage dealt - it could also include information on the type of damage, the source of the damage and so forth.  
The Health System would subscribe to the damage event but only for entities with a Health component.
So if the hit entity has a health component then the health system would receive the event and can react to it by subtracting health - potentially sending another event if the entity's health reaches zero.
Another System could handle the same damage events for entities with a location and physics component, to knock the entity away from the damage causer.

The advantage of using events rather than straight method calls between systems is that it decouples systems and provides the ability to add new systems in the future to react to the same event.
By sending a damage event rather than modifying the health component directly, it allows for systems that modify the damage being dealt based on arbitrary conditions, or cancel it, or display some sort of effect, or for the damage to be handled in a different way entirely without having to change the damage causer at all.

This provides two valuable features:

1. **react on events** It provides a mechanism for an entity to react to an event based on the components it has.
   This means that someone can later on create their own component and system combination that reacts to Damage events differently.
2. **intercept and modify events** Because an event can be received by multiple systems, it is possible for modders to intercept and modify or cancel an event to change behavior.
   An armor component could be added that halves all damage, and set up to intercept the damage event - without having to change the health system at all.

### Events in Detail

> 🚧 TODO: move this to sub-page or tutorial module

New events should extend AbstractEvent, which provides the event with the default cancellation functionality.

Systems that handle events should implement the EventHandler interface. They can then add new event handling methods by adding methods of the form:

```java
@ReceiveEvent(components = {HealthComponent.class, AnotherComponent.class}, priority = ReceiveEvent.PRIORITY_NORMAL)
public void onDamage(DamageEvent event, EntityRef entity) {
    // Do something
}
```

The method must have the signature `public void (T, EntityRef)`, where T is an type of Event - this determines which event will be sent to the method. The second parameter is the EntityRef for the entity that received the event. The name of the method and parameters can be anything, though I suggest starting the method name with "on", as in "onDamage". The method must also be annotated with @ReceiveEvent. The annotation must list what components an entity must have for the event handler method to be invoked - this allows you to filter out entities which are inappropriate for the event handler. You optionally may specify a priority, either using one of the preset priority levels or with an integer - this determines the order in which event handlers functions will be called when multiple are valid for a given event.

Events also support cancellation - this allows a system to stop an event before it reaches systems with a lower priority - for instance if you add an invincibility component to make an entity temporarily immune to damage, you can add a system that will intercept the Damage event and cancel it before it can reach the health system.

Inheritance structures of events are also supported - if you have a MouseButtonEvent, and a LeftMouseButtonEvent that inherits it, subscribing to MouseButtonEvent will also pick up LeftMouseButtonEvents.

## Example modules

Terasology's codebase can be daunting for somebody new to the project. Here are some examples that can hopefully provide some guidance for those who'd like to see how systems and components work in practice, starting from simpler modules and progressing to more complex ones.

1. **[Hunger](https://github.com/Terasology/Hunger)** (basic complexity) - simple component, single widget UI, simple event handling.
1. **[Journal](https://github.com/Terasology/Journal)** (intermediate) - simple component, composite widgets in UI, key binding, introduces events sent over network.
1. **[GrowingFlora](https://github.com/Terasology/GrowingFlora)** (advanced) - advanced components, interaction with world generation and use of scheduled events, introduces concept of chunk states, I'd suggest not looking too much into the tree shape and growth generation itself (implementation) just look at the interfaces.
1. **[Workstation](https://github.com/Terasology/Workstation)** (advanced) - use of components for defining flow, use of prefabs for defining new types of objects, complex interaction with inventory, both player and entity.
1. **[BlockNetwork](https://github.com/Terasology/BlockNetwork)** and **[Signalling](https://github.com/Terasology/Signalling)** (fairly complex) - maintains a separate data structure for loaded entities to improve performance, listens on Component, Entity and Chunk life-cycle events to keep the data structure up-to-date, complex logic, delay handling of events.

## Further Reading

- [Block Entity Behavior](http://forum.terasology.org/threads/block-entity-behavior.792)
- http://www.richardlord.net/blog/what-is-an-entity-framework
- http://www.richardlord.net/blog/why-use-an-entity-framework
- http://t-machine.org/index.php/2007/09/03/entity-systems-are-the-future-of-mmog-development-part-1/
