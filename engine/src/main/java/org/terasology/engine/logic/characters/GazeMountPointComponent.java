// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * The gaze mount point represents a character's "eyes".
 * <p>
 * It is attached to an entity, i.e., the location of the mount point is linked to the character's location.
 * <p>
 * The character location usually points to the center of the entity. The gaze mount point translation is relative to
 * that anchor location.
 */
public class GazeMountPointComponent implements Component<GazeMountPointComponent> {
    /**
     * Holds an entity that will have its location linked to the character entity.
     * <p>
     * Created and configured at runtime when this component is added.
     */
    @Owns
    @Replicate
    public EntityRef gazeEntity = EntityRef.NULL;

    /**
     * The translation vector relative to the gaze entity location.
     * <p>
     * The location usually points to the center of the entity.
     */
    @Replicate
    public Vector3f translate = new Vector3f(0, 0, 0);

    @Override
    public void copyFrom(GazeMountPointComponent other) {
        this.gazeEntity = other.gazeEntity;
        this.translate = new Vector3f(other.translate);
    }
}
