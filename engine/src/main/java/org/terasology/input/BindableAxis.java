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

package org.terasology.input;

/**
 * A Bind Axis is a (simulated) analog input axis, maintaining a value between -1 and 1.
 */
public interface BindableAxis {

    /**
     * @return The id of this axis
     */
    String getId();

    /**
     * Set the circumstance under which the axis will send events
     *
     * @param mode
     */
    void setSendEventMode(SendEventMode mode);

    /**
     * @return The circumstance under which the axis will send events
     */
    SendEventMode getSendEventMode();

    /**
     * Registers a direct subscriber to the axis events
     *
     * @param subscriber
     */
    void subscribe(BindAxisSubscriber subscriber);

    /**
     * Unregisters a direct subscriber to the axis events
     *
     * @param subscriber
     */
    void unsubscribe(BindAxisSubscriber subscriber);

    /**
     * @return The current value of the axis
     */
    float getValue();

}
