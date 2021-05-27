// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityRef;

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
