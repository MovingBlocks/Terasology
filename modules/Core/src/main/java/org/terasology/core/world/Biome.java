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
package org.terasology.core.world;

public enum Biome {
    MOUNTAINS(true, 0.95f), SNOW(false, 1.0f), DESERT(true, 0.0f), FOREST(true, 0.9f), PLAINS(true, 0.0f);

    private boolean vegetationFriendly;
    private float fog;

    private Biome(boolean vegetationFriendly, float fog) {
        this.vegetationFriendly = vegetationFriendly;
        this.fog = fog;
    }

    public boolean isVegetationFriendly() {
        return vegetationFriendly;
    }

    public float getFog() {
        return fog;
    }
}
