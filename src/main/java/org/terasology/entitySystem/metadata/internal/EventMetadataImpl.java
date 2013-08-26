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

package org.terasology.entitySystem.metadata.internal;

import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.copying.CopyStrategy;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;
import org.terasology.entitySystem.metadata.NetworkEventType;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.OwnerEvent;
import org.terasology.network.ServerEvent;

import java.lang.reflect.Field;

/**
 * @author Immortius
 */
public class EventMetadataImpl<T extends Event> extends ClassMetadataImpl<T> implements EventMetadata<T> {

    private NetworkEventType networkEventType = NetworkEventType.NONE;
    private boolean lagCompensated;
    private boolean skipInstigator;

    public EventMetadataImpl(Class<T> simpleClass, CopyStrategyLibrary copyStrategies, ReflectFactory factory, String uri) throws NoSuchMethodException {
        super(simpleClass, copyStrategies, factory, uri);
        if (simpleClass.getAnnotation(ServerEvent.class) != null) {
            networkEventType = NetworkEventType.SERVER;
            lagCompensated = simpleClass.getAnnotation(ServerEvent.class).lagCompensate();
        } else if (simpleClass.getAnnotation(OwnerEvent.class) != null) {
            networkEventType = NetworkEventType.OWNER;
        } else if (simpleClass.getAnnotation(BroadcastEvent.class) != null) {
            networkEventType = NetworkEventType.BROADCAST;
            skipInstigator = simpleClass.getAnnotation(BroadcastEvent.class).skipInstigator();
        }
    }

    @Override
    public boolean isNetworkEvent() {
        return networkEventType != NetworkEventType.NONE;
    }

    @Override
    public boolean isLagCompensated() {
        return lagCompensated;
    }

    @Override
    public boolean isSkipInstigator() {
        return skipInstigator;
    }

    @Override
    public NetworkEventType getNetworkEventType() {
        return networkEventType;
    }

    @Override
    protected <U> FieldMetadata<T, U> createField(Field field, CopyStrategy<U> typeHandler, ReflectFactory factory) {
        return new FieldMetadataImpl<>(this, field, typeHandler, factory, true);
    }
}
