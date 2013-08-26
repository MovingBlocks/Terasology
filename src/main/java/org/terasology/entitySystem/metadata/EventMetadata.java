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

/**
 * @author Immortius
 */
public interface EventMetadata<T> extends ClassMetadata<T> {

    /**
     * @return Whether this event is a network event.
     */
    boolean isNetworkEvent();

    /**
     * @return The type of network event this event is.
     */
    NetworkEventType getNetworkEventType();

    /**
     * @return Whether this event is compensated for lag.
     */
    boolean isLagCompensated();

    /**
     * @return Whether this event should not be replicated to the instigator
     */
    boolean isSkipInstigator();

}
