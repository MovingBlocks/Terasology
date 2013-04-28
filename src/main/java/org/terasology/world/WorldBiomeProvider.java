/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
        MOUNTAINS(true), SNOW(false), DESERT(true), FOREST(true), PLAINS(true), HEIGHTLANDS(true), NEWBIOME(true);

        private boolean vegetationFriendly;

        private Biome(boolean vegetationFriendly) {
            this.vegetationFriendly = vegetationFriendly;
        }

        public boolean isVegetationFriendly() {
            return vegetationFriendly;
        }
    }


    /**
     * Returns the humidity at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The humidity
     */
    public float getHumidityAt(int x, int z);

    /**
     * Returns the temperature at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The temperature
     */
    public float getTemperatureAt(int x, int z);

    public float getFog(float time);

    /*
    * Returns the biome type at the given position.
    */
    public Biome getBiomeAt(int x, int z);

    public Biome getBiomeAt(float x, float z);


}
