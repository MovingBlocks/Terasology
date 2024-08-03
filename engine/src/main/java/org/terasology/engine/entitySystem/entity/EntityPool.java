// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

public interface EntityPool {

    /**
     * Removes all entities from the pool.
     */
    void clear();

    /**
     * Creates an EntityBuilder.
     *
     * @return A new entity builder
     */
    EntityBuilder newBuilder();

    /**
     * Creates an EntityBuilder, from a prefab
     *
     * @return A new entity builder
     */
    EntityBuilder newBuilder(String prefabName);

    /**
     * Creates an EntityBuilder, from a prefab
     *
     * @return A new entity builder
     */
    EntityBuilder newBuilder(Prefab prefab);

    /**
     * @return A references to a new, unused entity
     */
    EntityRef create();

    /**
     * @return A references to a new, unused entity with the desired components
     */
    EntityRef create(Component... components);

    /**
     * @return A references to a new, unused entity with the desired components
     */
    EntityRef create(Iterable<Component> components);


    /**
     * Creates a new entity from the given components.
     *
     * @param components the components to create this entity from
     * @param sendLifecycleEvents will only send lifecycle events if this is true
     * @return
     */
    EntityRef create(Iterable<Component> components, boolean sendLifecycleEvents);

    /**
     * @param prefabName The name of the prefab to create.
     * @return A new entity, based on the the prefab of the given name. If the prefab doesn't exist, just a new entity.
     */
    EntityRef create(String prefabName);

    /**
     * @return A new entity, based on the given prefab
     */
    EntityRef create(Prefab prefab);

    // TODO: Review. Probably better to move these into a static helper

    /**
     * @return A new entity, based on the given prefab, at the desired position
     */
    EntityRef create(String prefab, Vector3fc position);


    /**
     * @return A new entity, based on the given prefab, at the desired position
     */
    EntityRef create(Prefab prefab, Vector3fc position);

    /**
     * @return A new entity, based on the given prefab, at the desired position, and with the desired rotation
     */
    EntityRef create(Prefab prefab, Vector3fc position, Quaternionfc rotation);

    /**
     * Creates an entity but doesn't send any lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @return The newly created entity ref.
     */
    EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components);

    /**
     * Creates an entity but doesn't send any lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @return The newly created entity ref.
     */
    EntityRef createEntityWithoutLifecycleEvents(String prefab);

    EntityRef createEntityWithoutLifecycleEvents(Prefab prefab);

    /**
     * Allows the creation of an entity with a given id - this is used
     * when loading persisted entities
     *
     * @return The entityRef for the newly created entity
     */
    EntityRef createEntityWithId(long id, Iterable<Component> components);

    /**
     * Retrieve the entity ref with the given id.
     *
     * @return the {@link EntityRef}, if it exists; {@link EntityRef#NULL} otherwise
     */
    EntityRef getEntity(long id);

    /**
     * @return an iterable over all of the entities in this pool
     */
    Iterable<EntityRef> getAllEntities();

    /**
     * All entities containing every one of the given components.
     * <p>
     * Implementation note:
     * Java generic types are a mess (see <a href="http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#Topic5">
     * Designing Generic Methods</a>), especially where varargs are involved.
     * We can't use {@link SafeVarargs @SafeVarargs} on any interface methods
     * because there is no way to know the <em>implementations</em> of the interface are safe.
     * <p>
     * In this case, in practice, there are few (if any) callers that pass more than two values.
     * By adding methods that explicitly take one and two values, we can present an interface the
     * compiler doesn't need to complain about at every call site.
     *
     * @return An iterable over all entities with the provided component types.
     */
    Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses);

    @SuppressWarnings("unchecked")
    default Iterable<EntityRef> getEntitiesWith(Class<? extends Component> componentClass) {
        return getEntitiesWith(new Class[] {componentClass});
    }

    @SuppressWarnings("unchecked")
    default Iterable<EntityRef> getEntitiesWith(Class<? extends Component> componentClass, Class<? extends Component> componentClass2) {
        return getEntitiesWith(new Class[] {componentClass, componentClass2});
    }

    /**
     * @return A count of entities with the provided component types
     */
    int getCountOfEntitiesWith(Class<? extends Component>... componentClasses);

    /**
     * @return A count of currently active entities
     */
    int getActiveEntityCount();

    /**
     * Does this pool contain the given entity?
     *
     * @param id the id of the entity to search for
     * @return true if this pool contains the entity; false otherwise
     */
    boolean contains(long id);
}
