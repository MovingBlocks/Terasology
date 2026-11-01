Events are signals which are send by systems against entities for communication and collaboration.

Events extend the entity component system (ECS) by explicit means of communication.
You can find events everywhere in the code base - they notify about completed tasks, cause sounds to be played, and allow for decoupled extension and modification of behavior.

> 💡 Keep in mind that events are processed synchronously, i.e., within the same tick of the game loop.

We can categorize events by their _intent_ on sender side (similar to design patterns).

- [Trigger Events](#trigger-events)
- [Notification Events](#notification-events)
- [Collector Events](#collector-events)

## Trigger Events

> Yo, please do this thing for me!

The intent of a _trigger event_ is to make another system perform a specific action.
You can see this event as a command or request.

An advantage of using trigger events is the reuse of functionality while maintaining only a loose coupling between modules.
A trigger event is a well-defined entry point for a process other systems can rely on.
Therefore, a module or system should describe its trigger events in its contract.
The sender is usually aware of (at least one) action that will be performed based on the event.

> ⚠️ Keep in mind that a sending a trigger event does not guarantee the execution of an action.

Examples for trigger events are inflicting damage to an entity (`DoDamageEvent`) or playing a specific sound asset (`PlaySoundEvent`).

**Trigger events are _immutable_.**
The content of the trigger event is fully defined by the sending system.
No other system can alter the content.
This ensures that, if the event reaches the system it is logically addressed to, it was not tampered with.

**Trigger events can be _consumable_.**
If a trigger event is consumable the command itself can be canceled before it reaches the target system.
Vice versa, if the event is _not consumable_ it is guaranteed to reach the target system.
If the [event flow] of an action offers a [Collector Event](#collector-events) the trigger should **not** be consumable. 

A simplified version of the mentioned `DoDamageEvent` may look like this.
The event is immutable and not consumable.

```java
public class DoDamageEvent implements Event {
    // private member, cannot be modified after event creation
    private final float amount;

    public DoDamageEvent(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }
}
```

When sending an instance of this trigger event to an entity, we want to express the intent of inflicting `amount` points of damage on an entity `entity`.

```java
entity.send(new DoDamageEvent(amount));
```

> 💡 The receiver system might specify additional requirements.
> For instance, damage might only be inflicted if the affected entity has a health component.
> These details should be stated in the module/system contract.

Trigger events are often named in active form, e.g., `CloseDoorEvent` or `PlaySoundEvent`.
Sometimes, the event name is prefixed with `Do…`, e.g., `DoDamageEvent`.
You may also encounter event names ending on `…RequestEvent`.

> 💡 When looking at trigger events from the receiver's perspective we can differentiate between _implicit_ and _explicit_ trigger events.
> 
> On the one hand, events which are deliberately sent by a system to trigger an action are _explicit triggers_. 
> On the other hand, a system can react to any [Notification Event](#notification-events) or observed change to start a process. 
> We consider these causes _implicit triggers_.


- 🔗 [Sending Events](Events-and-Systems.md#sending-events)

## Notification Events

> Hey, this thing happened.

The intent of a _notification event_ is to inform that something happened.
The sending system makes a statement about something it has observed or done.

Terasology comes with a basic set of [entity life cycle events]() which notify about state changes of entities (e.g., added, changed, or removed components).
This allows other systems to react on these changes - our foundation for game logic.

The reasons for dedicated notification events are manifold.
They can inform about the result of a process, a filtered view on a component change, or describe abstract events.
In all cases, there is a single source of truth assembling the notification event. 
This reduces code duplication, as systems can rely on notification events instead of computing the information themselves.

The sending system is unaware of event listeners.
Thus, it does not expect any action to follow the notification event.

Examples for notification events are life cycle events (e.g., `OnAddedComponent`), filtered views on component changes (e.g., `OnHealthChangedEvent`), enriched process results (e.g., `OnDamagedEvent`), or abstract events without (direct) representation in components (e.g., `OnBiomeChangedEvent`).

**Notification events are _immutable_.**
The sending system wants to convey specific information which should not be altered.
This ensures that all systems receive the same information.

**Notification events are _not consumable_.**
Notifications events inform about a completed action or event.
Consuming the event would withhold the information from other interested systems, and is seldom a good idea.

A simplified version of the notification event informing that an entity received damage may look like follows.
The event is immutable and not consumable.

```java
public class OnDamagedEvent implements Event {
    // private member, cannot be modified after event creation
    private final float amount;

    public OnDamagedEvent(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }
}
```

A system receiving an instance of this notification event can now react ot it.
For instance, the audio system plays a sound asset based on the amount of damage that was dealt.

> 💡 The notification event acts as an implicit [trigger event](#trigger-events).

Trigger events are often named in past tense, e.g. `MovedEvent`.
Many notification events are prefixed with `On…`, e.g., `OnDamagedEvent`.
Sometimes, they are named by the subject they describe, e.g, `CollisionEvent`.

- 🔗 [Processing Events](Events-and-Systems.md#processing-events)

## Collector Events

> I'm about to do a thing, any comments?

The intent of a _collector event_ is to ask systems for their contribution to an action.
The sending system is broadcasting a question to collect contributions from other interested systems.

Collector events are an extension mechanism to decouple external modifications from the base logic of an action.
The leading system performing an action owns the logic of that action.
The leading system offers controlled extension points via collector events.

An advantage of collector events is that they work well for temporary modifications.
To revert a modification the respective system simply stops contributing to the collector event. 

By pushing the logic for contributions to downstream systems we can avoid complex orchestration of contributions in the leading system.
The downstream systems act on the collector event independent of each other.

> To model explicit dependencies and collaboration between systems reacting to a collector event, consider a second level [event flow].

Examples for collector events are frequent actions (e.g., health regeneration or movement speed) and one-time events with (potentially many) contributing systems (e.g., damage affected by buffs and reductions).

**Collector events are _mutable_.**
Collector events are meant to be modified by downstream systems.
For numerical values the event usually extends `AbstractValueModifiableEvent`.
The event handler priority defines the precedence order in which downstream systems receive the event.

> Collector events may hold _immutable_ information as well to inform downstream systems about the context of the action.
> If the event is sent just to allow cancellation, there may be no mutable properties at all.

**Collector events are (often) _consumable_.**
Collector events are often consumable to allow a downstream system to cancel the action without any effect.
For more complex decision logic (e.g., majority vote) the collector event may be _not consumable_ but offer other means to express "cancellation".

A simplified version of the collector event to allow downstream systems to contribute to a damage action.
The event is partially mutable. It is consumable to allow for cancellation.

```java
public class BeforeDamage extends AbstractConsumableValueModifiableEvent {
    // private member, cannot be modified after event creation
    private final String damageType;

    public OnDamagedEvent(float baseDamage, String damageType) {
        // initialize the ValueModifiableEvent with the base damage value
        super(baseDamage);
        this.damageType = damageType;
    }

    // give additional context to downstream systems
    public String getDamageType() {
        return damageType;
    }
}
```

This event is sent against the entity that is about to receive damage.
A system reacting to this event can use the additional (immutable) context given by `getDamageType()` to influence the action, e.g., reduce the damage amount by 2 points if the damage type is `"pierceDamage"`.

> ⚠️ Collector events should never be used as notification events or trigger events as they can be canceled.

Collector events are often prefixed with `Before…`, `Affect…`, or `Get…`.

--- 

Note, that the categorization of events is ambiguous and depends on the point of view.
For instance, an event sent out as notification event by one system may be treated as trigger event by another system.
Some events may also fulfill the characteristics of several event types at once.

Read more on [event flow] to learn about typical use cases of the different types in a bigger picture.

<!-- References -->
[entity]: Glossary.md#entity
[event flow]: Event-Patterns.md