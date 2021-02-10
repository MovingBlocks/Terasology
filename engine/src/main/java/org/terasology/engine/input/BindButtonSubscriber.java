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

import org.terasology.entitySystem.entity.EntityRef;

/**
 * Interface for subscribing to bind button events
 *
 */
public interface BindButtonSubscriber {

    /**
     * Called when the bind is activated
     *
     * @param delta  The time passing this frame
     * @param target The current camera target
     * @return True if the bind event was consumed
     */
    boolean onPress(float delta, EntityRef target);

    /**
     * Called when the bind repeats
     *
     * @param delta  The time this frame (not per repeat)
     * @param target The current camera target
     * @return True if the bind event was consumed
     */
    boolean onRepeat(float delta, EntityRef target);

    /**
     * Called when the bind is deactivated
     *
     * @param delta  The time passing this frame
     * @param target The current camera target
     * @return True if the bind event was consumed
     */
    boolean onRelease(float delta, EntityRef target);
}
