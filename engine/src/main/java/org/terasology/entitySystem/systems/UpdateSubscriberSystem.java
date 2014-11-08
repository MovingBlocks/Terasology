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
package org.terasology.entitySystem.systems;

/**
 * Interface for component systems that (may) need to be updated every time
 * the engine is updated.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface UpdateSubscriberSystem extends ComponentSystem {

    /**
     * Update function for the Component System, which is called each
     * time the engine is updated.
     * @param delta The time (in seconds) since the last engine update.
     */
    void update(float delta);

}
