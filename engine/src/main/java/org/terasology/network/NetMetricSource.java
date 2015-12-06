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

package org.terasology.network;

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
