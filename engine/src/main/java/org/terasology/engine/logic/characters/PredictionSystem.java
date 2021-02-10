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

package org.terasology.logic.characters;

import org.terasology.entitySystem.entity.EntityRef;

/**
 * Interface for the system that provides the ability to compensate for lag, by rewinding and replaying state
 *
 */
public interface PredictionSystem {
    /**
     * Rewinds time for the specified client
     *
     * @param client The client entity to rewind for
     * @param timeMs The time to rewind to
     */
    void lagCompensate(EntityRef client, long timeMs);

    void restoreToPresent();
}
