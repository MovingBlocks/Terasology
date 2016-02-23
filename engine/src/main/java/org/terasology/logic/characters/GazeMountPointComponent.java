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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.Replicate;

public class GazeMountPointComponent implements Component {
    /**
     * Holds an entity that will have its location linked to the character entity.  Created and configured at runtime when this component is added
     */
    @Owns
    @Replicate
    public EntityRef gazeEntity = EntityRef.NULL;
    @Replicate
    public Vector3f translate = new Vector3f(0, 0, 0);
}
