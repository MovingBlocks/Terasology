// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import com.google.common.collect.Lists;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.event.internal.EventReceiver;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Test helper for listening to {@link Event}s
 * <p>
 * A test should instantiate a {@code TestEventReceiver}, execute some code, and then examine the list of entityRefs or
 * events provided by {@link #getEntityRefs()} and {@link #getEvents()}.
 * <p>
 * The receiver automatically collects events of the given type sent to its {@link Context}.
 *
 * <pre>
 * {@code
 * TestEventReceiver<DropItemEvent> dropReceiver = new TestEventReceiver<>(getHostContext(), DropItemEvent.class)
 * // fire some events
 * for (DropItemEvent event : dropReceiver.getEvents()) {
 *   // do something with the events
 * }
 * }
 * </pre>
 * Users can optionally supply a {@link BiConsumer} to handle the events with custom logic.
 * <pre>
 * {@code
 * TestEventReceiver receiver = new TestEventReceiver<>(context, DropItemEvent.class, (event, entity) -> {
 *   // do something with the event or entity
 * });
 * }
 * </pre>
 * Additionally, a list of required <em>component types</em> can be passed to the event receiver. This is equivalent
 * to listing the components in the {@code ReceiveEvent(components = {...}} block of a regular event handler.
 * <pre>
 * {@code
 * TestEventReceiver receiver = new TestEventReceiver<>(context, DropItemEvent.class, (event, entity) -> {
 *   // do something with the event or entity
 * }, MagicItemComponent.class);
 * }
 * </pre>
 * <p>
 * You can automatically unregister your receiver using a try-with-resources block ({@code TestEventReceiver} is {@link
 * AutoCloseable}):
 *
 * <pre>
 * {@code
 * try (TestEventReceiver<DropItemEvent> spy = new TestEventReceiver<>(getHostContext(), DropItemEvent.class)) {
 *   drops = spy.getEntityRefs();
 * }
 * }
 * </pre>
 * <p>
 * Note that listeners are discarded with the rest of the engine between tests, so closing your receiver is only useful
 * if you need to stop handling events within a single test method.
 */
public class TestEventReceiver<T extends Event> implements AutoCloseable, EventReceiver<T> {
    private final EventSystem eventSystem;
    private final Class<T> eventClass;
    private final BiConsumer<T, EntityRef> handler;

    private final List<EntityRef> entityRefs = new ArrayList<>();
    private final List<T> events = new ArrayList<>();

    /**
     * Constructs a new {@code TestEventReceiver} and registers it to listen for events.
     * <p>
     * The following signature of a {@code TestEventReceiver} is equivalent to the event handler below:
     * <pre>
     * TestEventReceiver receiver = new TestEventReceiver&lt;&gt;(context, MyEvent.class, (event, entity) -&gt; {
     *   // do something with the event or entity
     * }, MyComponent.class);
     *
     * // ... corresponds to
     *
     * &#64;ReceiveEvent(components = {MyComponent.class})
     * public void handler(MyEvent event, EntityRef entity) {
     *     // do something with the event or entity
     * }
     * </pre>
     *
     * @param context the context object for the test; this should probably be obtained through {@link
     *         ModuleTestingHelper#createClient()} and is needed so we can obtain an {@link EventSystem} instance to
     *         register our event handler.
     * @param eventClass the {@link Event} subclass to listen for
     * @param handler an optional {@link BiConsumer} fired when events are received
     * @param componentTypes list of component types that need to be present on the entity receiving the event
     */
    public TestEventReceiver(Context context, Class<T> eventClass, BiConsumer<T, EntityRef> handler, Class<?
            extends Component>... componentTypes) {
        this.eventClass = eventClass;
        this.handler = handler;
        eventSystem = context.get(EventSystem.class);

        Class<? extends Component>[] components =
                Lists.asList(EntityInfoComponent.class, componentTypes).toArray(new Class[componentTypes.length + 1]);

        eventSystem.registerEventReceiver(this, eventClass, components);
    }

    /**
     * @see #TestEventReceiver(Context, Class, BiConsumer, Class[])
     */
    public TestEventReceiver(Context context, Class<T> eventClass) {
        this(context, eventClass, (event, entity) -> {
        });
    }

    /**
     * Unregisters this {@code TestEventReceiver} so it stops listening for events.
     */
    public void close() {
        eventSystem.unregisterEventReceiver(this, eventClass, EntityInfoComponent.class);
    }

    /**
     * Returns a read-only view of the list of entities which are sent events.
     * <p>
     * Note that entities appear in the order they received the events, and may appear multiple times. Each entity
     * corresponds to the {@link #getEvents()} member with the same index.
     * <p>
     * If the {@code TestEventReceiver} has not been {@linkplain #close() closed}, then this list will continue to be
     * updated if further events occur.
     */
    public List<EntityRef> getEntityRefs() {
        return Collections.unmodifiableList(entityRefs);
    }

    /**
     * Returns a read-only view of the list of events.
     * <p>
     * If the {@code TestEventReceiver} has not been {@linkplain #close() closed}, then this list will continue to be
     * updated if further events occur.
     */
    public List<T> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Records the event.
     * <p>
     * Note that this doesn't put the entity in an inventory or otherwise interfere with the event itself, but it does
     * store a reference to the entity and event.  Consequently, the entity still exists in the world, and if other
     * actors modify or destroy it, those changes would be reflected in the list of entityRefs.
     */
    public void onEvent(T event, EntityRef entity) {
        handler.accept(event, entity);
        events.add(event);
        entityRefs.add(entity);
    }
}
