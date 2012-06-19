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
public interface BindableButton {

    /**
     * @return The identifier for this button
     */
    String getId();

    /**
     * @return The display name for this button
     */
    String getDisplayName();

    /**
     * Set the circumstance under which this button sends events
     *
     * @param mode
     */
    void setMode(ActivateMode mode);

    /**
     * @return The circumstance under which this button sends events
     */
    ActivateMode getMode();

    /**
     * Sets whether this button sends repeat events while pressed
     *
     * @param repeating
     */
    void setRepeating(boolean repeating);

    /**
     * @return Whether this button sends repeat events while pressed
     */
    boolean isRepeating();

    /**
     * @param repeatTimeMs The time (in milliseconds) between repeat events being sent
     */
    void setRepeatTime(int repeatTimeMs);

    /**
     * @return The time (in milliseconds) between repeat events being sent
     */
    int getRepeatTime();

    /**
     * @return The current state of this button (either up or down)
     */
    ButtonState getState();

    /**
     * Used to directly subscribe to the button's events
     *
     * @param subscriber
     */
    void subscribe(BindButtonSubscriber subscriber);

    /**
     * Used to unsubscribe from the button's event
     *
     * @param subscriber
     */
    void unsubscribe(BindButtonSubscriber subscriber);

    public static enum ActivateMode {
        /**
         * The button will only send ButtonState.DOWN events
         */
        PRESS(true, false),
        /**
         * The button will only send ButtonState.UP events
         */
        RELEASE(false, true),
        /**
         * The button will send all events
         */
        BOTH(true, true);

        private boolean activatedOnPress;
        private boolean activatedOnRelease;

        private ActivateMode(boolean activatedOnPress, boolean activatedOnRelease) {
            this.activatedOnPress = activatedOnPress;
            this.activatedOnRelease = activatedOnRelease;
        }

        public boolean isActivatedOnPress() {
            return activatedOnPress;
        }

        public boolean isActivatedOnRelease() {
            return activatedOnRelease;
        }
    }
}
