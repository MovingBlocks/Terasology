// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

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
