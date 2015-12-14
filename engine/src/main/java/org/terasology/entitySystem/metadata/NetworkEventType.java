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
 */
public enum NetworkEventType {

    /**
     * This event is not a network event
     */
    NONE,

    /**
     * This event is propagated from client to server
     */
    SERVER,

    /**
     * This event is propagated from the server to the client that owns the target entity
     */
    OWNER,

    /**
     * This event is propagated from the server to all clients.
     */
    BROADCAST
}
