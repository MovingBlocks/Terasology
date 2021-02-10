/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.players;

import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * This is attached to the player entities in order to manually set a custom spawn location.
 */
public class StaticSpawnLocationComponent implements Component {
    @Replicate
    public Vector3f position;
}
