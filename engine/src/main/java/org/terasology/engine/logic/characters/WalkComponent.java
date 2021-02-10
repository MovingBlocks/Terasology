/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.animation.MeshAnimation;

import java.util.List;

/**
 * The component gets currently only used as container for the walk animations,
 * but will in future also store the walk speed.
 *
 */
public class WalkComponent implements Component {
    /**
     * A pool of walk animations. It gets currently only used by behavior trees to make a skeletal mesh perform a
     * walk animation loop. The animations of the pool will be picked by random. The result is a randomized animation
     * loop. The same animation can be put multiple times in the pool, so that it will be chosen more frequently.
     */
    public List<MeshAnimation> animationPool = Lists.newArrayList();
}
