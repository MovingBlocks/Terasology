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
package org.terasology.logic.particles;

import org.terasology.math.geom.Vector3f;

/**
 * Functor object for inspection/tweaking of the data of a particle.
 * @author Linus van Elswijk
 */
public interface ParticleSpawnFunction {

    /**
     * Function callback.
     * @param inOutData Input and output parameter of the particle data
     */
    void call(final ParticleData inOutData);
}
