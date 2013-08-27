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

import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.OwnerEvent;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
public class EventMetadata<T extends Event> extends ClassMetadata<T> {

    private NetworkEventType networkEventType = NetworkEventType.NONE;
    private boolean lagCompensated;
    private boolean skipInstigator;

    public EventMetadata(Class<T> simpleClass, CopyStrategyLibrary copyStrategies, ReflectFactory factory, String uri) throws NoSuchMethodException {
        super(simpleClass, factory, copyStrategies, uri);
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

}
