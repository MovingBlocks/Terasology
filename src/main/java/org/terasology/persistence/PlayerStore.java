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
    public String getId();

    /**
     * Saves the store - call this when you have finished setting up the store.
     */
    public void save();

    /**
     * Stores the character, unloading it from the world
     * @param character
     */
    public void storeCharacter(EntityRef character);

    /**
     * Restores the player's character, loading it into the entity system
     * @return The restored character's EntityRef
     */
    public EntityRef restoreCharacter();

    /**
     * Sets the location which should be loaded for the player when they rejoin the game.
     * <p/>
     * This is set automatically to the character's location if a character is stored.
     *
     * @param location
     */
    public void setRelevanceLocation(Vector3f location);

    /**
     * @return The location that is the center of the area relevant for the player.
     */
    public Vector3f getRelevanceLocation();

    /**
     * @return Whether the player has a character or not
     */
    public boolean hasCharacter();


}
