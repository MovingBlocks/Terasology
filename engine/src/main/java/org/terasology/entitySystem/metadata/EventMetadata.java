/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.entitySystem.metadata;

import com.google.common.base.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.OwnerEvent;
import org.terasology.network.ServerEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 */
public class EventMetadata<T extends Event> extends ClassMetadata<T, ReplicatedFieldMetadata<T, ?>> {
    private static final Logger logger = LoggerFactory.getLogger(EventMetadata.class);

    private NetworkEventType networkEventType = NetworkEventType.NONE;
    private boolean lagCompensated;
    private boolean skipInstigator;

    public EventMetadata(Class<T> simpleClass, CopyStrategyLibrary copyStrategies, ReflectFactory factory, SimpleUri uri) throws NoSuchMethodException {
        super(uri, simpleClass, factory, copyStrategies, Predicates.<Field>alwaysTrue());
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
            logger.error("Event '{}' is a network event but lacks a default constructor - will not be replicated", this);
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
    protected <V> ReplicatedFieldMetadata<T, ?> createField(Field field, CopyStrategy<V> copyStrategy, ReflectFactory factory) throws InaccessibleFieldException {
        return new ReplicatedFieldMetadata<>(this, field, copyStrategy, factory, true);
    }
}
