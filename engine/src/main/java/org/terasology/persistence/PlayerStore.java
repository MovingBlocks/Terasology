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
package org.terasology.persistence;

import org.joml.Vector3fc;
import org.terasology.entitySystem.entity.EntityRef;

/**
 */
public interface PlayerStore {

    /**
     * @return The id of this store
     */
    String getId();

    /**
     * Restores all entities contained in this store, activating them.
     */
    void restoreEntities();

    /**
     * Stores the character
     *
     * @param character
     */
    void setCharacter(EntityRef character);

    /**
     * Retrieves the player's character entity
     *
     * @return The restored character's EntityRef
     */
    EntityRef getCharacter();

    /**
     * Sets the location which should be loaded for the player when they rejoin the game.
     * <br><br>
     * This is set automatically to the character's location if a character is stored.
     *
     * @param location
     */
    void setRelevanceLocation(Vector3fc location);

    /**
     * @return The location that is the center of the area relevant for the player.
     */
    Vector3fc getRelevanceLocation();

    /**
     * @return Whether the player has a character or not
     */
    boolean hasCharacter();


}
