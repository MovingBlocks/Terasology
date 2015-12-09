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
 */
public interface ComponentSystem {
    /**
     * Called to initialise the system. This occurs after injection, but before other systems are necessarily initialised, so they should not be interacted with
     */
    void initialise();

    /**
     * Called after all systems are initialised, but before the game is loaded
     */
    void preBegin();

    /**
     * Called after the game is loaded, right before first frame
     */
    void postBegin();

    /**
     * Called before the game is saved (this may be after shutdown)
     */
    void preSave();

    /**
     * Called after the game is saved
     */
    void postSave();

    /**
     * Called right before the game is shut down
     */
    void shutdown();
}
