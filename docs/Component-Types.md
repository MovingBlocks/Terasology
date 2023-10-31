Components are data objects that define the state of an entity and can optionally provide configuration information. They are used by engine and module systems to determine behaviors to apply. Components can be added and removed statically to prefabs or dynamically to entities in-game.

Components extend the entity component system (ECS) by explicit means of providing state and configuration data.
You can find components everywhere in the code base - they inform, for instance, about blocks being damaged, audio files to be played for footsteps, or the contents of the player's starting inventory.

> :construction: **_Note, this concept page is currently still under construction. This means that the elaborated concepts and their details are still subject to change until further notice._**

> _**TODO:** Component fields or entire components are "owned" by a module-internal system that is responsible for managing the field or component._

> _**TODO:** Generally, we advise to use topic components to group together related configuration and state information. Separating individual aspects into dedicated components can be useful to allow event receivers to filter which events to process. Before separation, it is advised to consider whether the filtering can also be achieved with a combination of already existing components. For example, ..._

We can categorize events by the data they contain.

- [Marker Components](#marker-components)
- [Configuration Components](#configuration-components)
- [Topic Components](#topic-components)

Furthermore, components can be _internal_. Internal components should only be managed by their associated system. Any write accesses from module-external systems should be avoided.

## Marker Components

> Yo btw, that thing is in a state!

A _marker component_ represents a single binary information. It marks an entity as having a certain property or state. This information can be used by systems to inform about which actions to take for this entity. If the component is not present, the entity does not have the respective property or state (yet or anymore).

An example for a marker component are blocks or block-like entities being in a damaged state (`BlockDamagedComponent`).

**Marker components _do not contain configuration or further state_.**
Marker components are intended to act as indicators of one specific property or state.
As such, they are not expected to provide any additional configuration or state data.

## Configuration Component

> Hey, this thing should look and act like that.

A _configuration component_ stores settings or parameters that can be modified by the player or the game itself.
Systems apply these settings or parameters to the in-game representation of the entity.

Examples for configuration components are setting the non-technical name of an entity that will be displayed to the player (`DisplayNameComponent`) or configuring the sound to play when the player interacts with the entity (`PlaySoundActionComponent`).

**Configuration components only contain _configuration data_.**
Configuration components are intended to provide specific settings and parameters for an entity.
They are not expected to provide state data.

## Topic Components

> Here's all relevant data about that topic.

A _topic component_ combines configuration and state data to provide systems with all the data they need.
It groups data relevant for a specific topic into a single component.

An example for a topic component is the `HealthComponent` which contains both, the maximum and current health of an entity.
While maximum health is configuration data that for instance allows to determine when the entity is fully healed again, the current health is state data that, when compared to maximum health, allows a system to determine whether or not an entity is damaged or even needs to be destroyed.

--- 

<!-- References -->
[entity]: Glossary.md#entity