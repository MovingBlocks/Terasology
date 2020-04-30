/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.core.logic.actions;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.health.EngineDamageTypes;

public class ArrowActionComponent implements Component {

    /**
     * The max distance the arrow will fly.
     */
    public int maxDistance = 24;

    /**
     * The damage the arrow does
     */
    public int damageAmount = 3;

    /**
     * How many arrows can be fired per second
     */
    public float arrowsPerSecond = 1.0f;

    public Prefab damageType = EngineDamageTypes.PHYSICAL.get();

}
