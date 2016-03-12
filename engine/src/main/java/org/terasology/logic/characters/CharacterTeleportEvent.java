/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.characters;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;

/**
 * Used within the server to trigger a teleport of a character. Just chaining the position is not possible due to
 * movement prediction.
 */
public class CharacterTeleportEvent implements Event {
    private Vector3f targetPosition;

    public CharacterTeleportEvent(Vector3f targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Vector3f getTargetPosition() {
        return targetPosition;
    }
}
