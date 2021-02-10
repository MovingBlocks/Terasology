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

package org.terasology.utilities.procedural;

/**
 * An abstract implementation of most methods.
 * The int-based methods delegate to float-bases ones.
 */
public abstract class AbstractNoise implements Noise {

    @Override
    public float noise(int x, int y) {
        return noise((float) x, (float) y);
    }

    @Override
    public float noise(int x, int y, int z) {
        return noise((float) x, (float) y, (float) z);
    }

    @Override
    public float noise(float x, float y) {
        return noise(x, y, 0);
    }
}
