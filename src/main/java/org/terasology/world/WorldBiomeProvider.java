/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world;

/**
 * @author Immortius
 */
public interface WorldBiomeProvider {

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


    /**
     * Returns the humidity at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The humidity
     */
    float getHumidityAt(int x, int z);

    /**
     * Returns the temperature at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The temperature
     */
    float getTemperatureAt(int x, int z);

    float getFog(float x, float y, float z);

    /*
    * Returns the biome type at the given position.
    */
    Biome getBiomeAt(int x, int z);

    Biome getBiomeAt(float x, float z);


}
