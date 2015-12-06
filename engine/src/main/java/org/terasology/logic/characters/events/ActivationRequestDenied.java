/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.event.Event;

/**
 * This event is attached to the character.
 *
 * It is sent when a {@link ActivationRequest} was not acceptable by the server.
 *
 * Modules are allowed to process the event, but should not sent it.
 *
 * This is an authority internal event. If a reaction should be sent the client is up to the systems that process this
 * event..
 *
 */
public class ActivationRequestDenied implements Event {
    private int activationId;

    protected ActivationRequestDenied() {
    }

    public ActivationRequestDenied(int activationId) {
        this.activationId = activationId;
    }

    public int getActivationId() {
        return activationId;
    }
}
