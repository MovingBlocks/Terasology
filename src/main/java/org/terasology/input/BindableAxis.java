/*
 * Copyright 2012
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
 * @author Immortius
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

    public enum SendEventMode {
        /**
         * Send an event every update/frame with the current axis value
         */
        ALWAYS {
            @Override
            public boolean shouldSendEvent(float oldValue, float newValue) {
                return true;
            }
        },
        /**
         * Sends an event every frame in which the current axis value is not zero
         */
        WHEN_NON_ZERO {
            @Override
            public boolean shouldSendEvent(float oldValue, float newValue) {
                return newValue != 0;
            }
        },
        /**
         * Only sends an event when the value of the axis changes
         */
        WHEN_CHANGED {
            @Override
            public boolean shouldSendEvent(float oldValue, float newValue) {
                return oldValue != newValue;
            }
        };

        public abstract boolean shouldSendEvent(float oldValue, float newValue);
    }
}
