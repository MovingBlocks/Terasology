/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public interface PlayerStore {

    /**
     * @return The id of this store
     */
    String getId();

    /**
     * Saves the store - call this when you have finished setting up the store. When this is called all entities
     * in the store will be deactivated.
     */
    void save();

    /**
     * Saves the store - call this when you have finished setting up the store.
     * @param deactivateEntities Whether the stored entities should be deactivated
     */
    void save(boolean deactivateEntities);

    /**
     * Restores all entities contained in this store, activating them.
     */
    void restoreEntities();

    /**
     * Stores the character
     * @param character
     */
    void setCharacter(EntityRef character);

    /**
     * Retrieves the player's character entity
     * @return The restored character's EntityRef
     */
    EntityRef getCharacter();

    /**
     * Sets the location which should be loaded for the player when they rejoin the game.
     * <p/>
     * This is set automatically to the character's location if a character is stored.
     *
     * @param location
     */
    void setRelevanceLocation(Vector3f location);

    /**
     * @return The location that is the center of the area relevant for the player.
     */
    Vector3f getRelevanceLocation();

    /**
     * @return Whether the player has a character or not
     */
    boolean hasCharacter();


}
