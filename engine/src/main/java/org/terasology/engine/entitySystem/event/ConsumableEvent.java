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
package org.terasology.entitySystem.event;

/**
 * A consumable event is an event that can be prevented from continuing through remaining event receivers. This is
 * primarily useful for input event.
 *
 */
public interface ConsumableEvent extends Event {

    /**
     * Tells whether or not the Event has been consumed.
     * @return true if the the event has been consumed, false otherwise.
     */
    boolean isConsumed();

    /**
     * Marks the Event as consumed.
     * Makes subsequent {@link #isConsumed()} calls return true.
     */
    void consume();
}
