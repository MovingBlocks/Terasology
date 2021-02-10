/*
 * Copyright 2015 MovingBlocks
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

import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.Replicate;

/**
 * The gaze mount point represents a character's "eyes".
 * <p>
 * It is attached to an entity, i.e., the location of the mount point is linked to the character's location.
 * <p>
 * The character location usually points to the center of the entity. The gaze mount point translation is relative to
 * that anchor location.
 */
public class GazeMountPointComponent implements Component {
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
}
