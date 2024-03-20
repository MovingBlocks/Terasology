// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import com.google.common.base.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class EventMetadata<T extends Event> extends ClassMetadata<T, ReplicatedFieldMetadata<T, ?>> {
    private static final Logger logger = LoggerFactory.getLogger(EventMetadata.class);

    private NetworkEventType networkEventType = NetworkEventType.NONE;
    private boolean lagCompensated;
    private boolean skipInstigator;

    public EventMetadata(Class<T> simpleClass, CopyStrategyLibrary copyStrategies, ReflectFactory factory, ResourceUrn uri)
            throws NoSuchMethodException {
        super(uri.toString(), simpleClass, factory, copyStrategies, Predicates.<Field>alwaysTrue());
        if (simpleClass.getAnnotation(ServerEvent.class) != null) {
            networkEventType = NetworkEventType.SERVER;
            lagCompensated = simpleClass.getAnnotation(ServerEvent.class).lagCompensate();
        } else if (simpleClass.getAnnotation(OwnerEvent.class) != null) {
            networkEventType = NetworkEventType.OWNER;
        } else if (simpleClass.getAnnotation(BroadcastEvent.class) != null) {
            networkEventType = NetworkEventType.BROADCAST;
            skipInstigator = simpleClass.getAnnotation(BroadcastEvent.class).skipInstigator();
        }
        if (networkEventType != NetworkEventType.NONE && !isConstructable() && !Modifier.isAbstract(simpleClass.getModifiers())) {
            logger.error("Event '{}' is a network event but lacks a default constructor - will not be replicated", this); //NOPMD
        }
    }

    /**
     * @return Whether this event is a network event.
     */
    public boolean isNetworkEvent() {
        return networkEventType != NetworkEventType.NONE;
    }

    /**
     * @return The type of network event this event is.
     */
    public NetworkEventType getNetworkEventType() {
        return networkEventType;
    }

    /**
     * @return Whether this event is compensated for lag.
     */
    public boolean isLagCompensated() {
        return lagCompensated;
    }

    /**
     * @return Whether this event should not be replicated to the instigator
     */
    public boolean isSkipInstigator() {
        return skipInstigator;
    }

    @Override
    protected ReplicatedFieldMetadata<T, ?> createField(Field field, CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory)
            throws InaccessibleFieldException {
        return new ReplicatedFieldMetadata<>(this, field, copyStrategyLibrary, factory, true);
    }
}
