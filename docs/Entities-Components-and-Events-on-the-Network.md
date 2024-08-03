Unlike in single player, the client and the server reside on different instances in multiplayer. Therefore, components and entities have to be correctly configured to ensure that they work as intended in a multiplayer setting. Often, this means annotating the relevant fields with `@Replicate`.

`@Replicate` is an annotation used to mark types or fields to indicate that these types or fields should be replicated between the server and the client. In other words, when changes are made to these types or fields on the server, these changes will be reflected in the clients as well.

This page will focus on component fields and how they can be replicated by the network. However, take note that `@Replicate` can be applied on component classes as well. This is especially important for ensuring that empty components are correctly replicated. Currently the Replicate annotation on the component does not get used as default for the fields. So fields need to be marked for replication if you want them to be replicated.

To illustrate how `@Replicate` can be used, let's take a look at a field in `ItemComponent`:

```java
/**
* How many of said item are there in this stack
*/
@Replicate(FieldReplicateType.SERVER_TO_CLIENT)
public byte stackCount = 1;
```

The `stackCount` field in `ItemComponent` specifies the amount of items in the current stack. 

As you can see, under the `@Replicate` annotation, the `FieldReplicateType` element is set to **SERVER_TO_CLIENT**. This annotation ensures that this when the value of the `stackCount` field of `ItemComponent` is updated on the server (i.e. when the stack size of the item changes), its value on all connected clients will be updated as well. Obviously very important as the number of items in a stack should be always be updated on all clients!

Apart from **SERVER_TO_CLIENT**, there are also a few other values for `FieldReplicateType` that determine the circumstances under which the field will be replicated. 

 `FieldReplicateType` | Description
--------|-------------
**SERVER_TO_CLIENT**   | The field will be replicated by the server to all clients connected to the server. This is the default `FieldReplicateType`.
**SERVER_TO_OWNER**    | The field will be replicated by the server to the client only if the client is the owner (i.e. the client belongs to the entity containing the component).
**OWNER_TO_SERVER**    | Functionally the same as **OWNER_TO_SERVER_TO_CLIENT**
**OWNER_TO_SERVER_TO_CLIENT**  | The field will be replicated from the owner to the server. It will be then be replicated by the server to all connected clients that are not the owner.

Apart from `FieldReplicateType`, you can also specify the value of the `initialOnly` element, which is false by default. When set to true, the field will only be replicated once when the entity containing the component first becomes relevant to the client. 

For instance in `ItemComponent`, it is used in `maxStackSize`:
```java
@Replicate(value = FieldReplicateType.SERVER_TO_CLIENT, initialOnly = true)
public byte maxStackSize = 99;
```

Unlike `stackSize`, which might change over the course of a game as the player receives or uses the item, the `maxStackSize` of an item does not change. Therefore, the `initialOnly` element is set to true as the value of `maxStackSize` only needs to be replicated by the server to the client once when the `ItemComponent` first becomes relevant.

To summarise, the server will send replicated fields only when:

1. It is the initial send of the component field
2. The field is replicated from Server to Client
3. The field is replicated from Server to Owner and the client owns the entity
4. The field is replicated from owner and the client doesn't own the entity  

The exception to this is when `initialOnly` is set to true and it isn't the inital send of the component field.

**Take note**: There is also the `@NoReplicate` annotation, which is the opposite of `@Replicate` annotation. It specifies that a component field should **not** be replicated. By default, all fields except Event fields are not replicated.

**Take note**: Don't forget to use `entityRef.saveComponent(component)` to save change of value in the component, or the change will not replicate.

## NetworkComponent

However, for updates to component fields of an entity to be replicated in a server, the entity needs to be registered on the network, which is where `NetworkComponent` comes into the picture. 

When `NetworkSystem` is first initialised, all entities containing a `NetworkComponent` are registered on the network as network entities and given a network ID. While entities might have different IDs each time, network entities are linked to their respective entities through the network IDs, allowing these entities to survive dropping in and out of relevance. 

Similar to `FieldReplicateType`, the `ReplicateMode` enum determines which clients the entity should be replicated to (i.e. which clients the entity is registered on).

 `ReplicateMode` | Description
--------|-------------
**ALWAYS** | The entity will always replicated to all clients connected to the server.
**RELEVANT** | The entity will only be replicated to clients where it was relevant (within a certain distance). This is the default value.
**OWNER** | The entity will always be replicated to its owner.

An example whereby both the `@Replicate` annotation and `NetworkComponent` are used is in the chest.

Chests store their items in `InventoryComponent`, in the following List:
```java
@Replicate
@Owns
public List<EntityRef> itemSlots = Lists.newArrayList();
```

Again, the `@Replicate` annotation ensures that whenever the value of the component field is updated on the server, this change will be reflected in all clients as well (recall that the default value of `FieldReplicateType` is **SERVER_TO_CLIENT**). In other words, whenever a player modifies the items in the chest, others in the same server will be able to see this change.

However, if the chest entity is not registered on the network, not all clients connected to the server might recognise the chest entity, preventing them from interacting with it. This is why `NetworkComponent` is specified in `chest.prefab` as well:

```javascript
...
"Network": {
}
...
```

Recall that the default `ReplicateMode` is **RELEVANT**. This `NetworkComponent` thus ensures that the chest entity will always be replicated by the server to a client whenever it is relevant to the client, ensuring that all interactions with the chest work as intended.

## @ServerEvent

As mentioned previously, event fields are all replicated by default. 

However, for events that are consumed by the server, they have to be marked with the `@ServerEvent` annotation. When events marked with `@ServerEvent` are sent on the client, they will be replicated to the server as well so that they can be actioned there.

This is very important for events that require action on the part of the server, such as `AbstractMoveItemRequest` and `DropItemRequest`.

```java
@ServerEvent
public abstract class AbstractMoveItemRequest implements Event {
...
```

```java
@ServerEvent(lagCompensate = true)
public class DropItemRequest implements Event {
...
```

You can also specify the `lagCompensate` element when marking events with the `@ServerEvent` annotation, as seen from `DropItemEvent` It is false by default.  If set to true however, the positioning of all characters on the server will be rewound to simulate the conditions on the client when the event was initially sent before the event is processed by the server. 

In the case of `DropItemRequest`, there is a need for `lagCompensate` to be set to true as the item should be dropped at the position where the character was when the request was initially sent, rather than the position where the character is when the event is received by the server. This thus takes into account the time taken for the request to be sent from the client to the server.