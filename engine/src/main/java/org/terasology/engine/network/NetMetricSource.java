// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

/**
 * Interface for providers of sources of network metric information
 *
 */
/* TODO: Produce a better metric system that doesn't rely on a single user calling it at set intervals (metric information
   will be useful for dynamic allocation of network bandwidth */
public interface NetMetricSource {
    /**
     * @return The amount of messages received since last time this method was called
     */
    int getReceivedMessagesSinceLastCall();

    /**
     * @return The amount of bytes of data received since last this this method was called
     */
    int getReceivedBytesSinceLastCall();

    /**
     * @return The amount of messages sent since last time this method was called
     */
    int getSentMessagesSinceLastCall();

    /**
     * @return The amount of bytes sent since last time this method was called
     */
    int getSentBytesSinceLastCall();
}
