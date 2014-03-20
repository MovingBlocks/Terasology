/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.core.config;

import org.terasology.world.WorldBiomeProvider;

/**
 * @author Immortius
 */
public class WorldGenerationConfig {
    private float forestGrassDensity = 0.3f;
    private float plainsGrassDensity = 0.2f;
    private float snowGrassDensity = 0.001f;
    private float mountainGrassDensity = 0.2f;
    private float desertGrassDensity = 0.001f;
    private float coalDensity = 0.050f;
    private float ironDensity = 0.010f;
    private float copperDensity = 0.010f;
    private float goldDensity = 0.005f;
    private float diamondDensity = 0.001f;
    private float gravelDensity = 0.050f;

    public float getForestGrassDensity() {
        return forestGrassDensity;
    }

    public void setForestGrassDensity(float forestGrassDensity) {
        this.forestGrassDensity = forestGrassDensity;
    }

    public float getPlainsGrassDensity() {
        return plainsGrassDensity;
    }

    public void setPlainsGrassDensity(float plainsGrassDensity) {
        this.plainsGrassDensity = plainsGrassDensity;
    }

    public float getSnowGrassDensity() {
        return snowGrassDensity;
    }

    public void setSnowGrassDensity(float snowGrassDensity) {
        this.snowGrassDensity = snowGrassDensity;
    }

    public float getMountainGrassDensity() {
        return mountainGrassDensity;
    }

    public void setMountainGrassDensity(float mountainGrassDensity) {
        this.mountainGrassDensity = mountainGrassDensity;
    }

    public float getDesertGrassDensity() {
        return desertGrassDensity;
    }

    public void setDesertGrassDensity(float desertGrassDensity) {
        this.desertGrassDensity = desertGrassDensity;
    }

    public float getGrassDensity(WorldBiomeProvider.Biome biome) {
        switch (biome) {
            case PLAINS:
                return getPlainsGrassDensity();
            case MOUNTAINS:
                return getMountainGrassDensity();
            case FOREST:
                return getForestGrassDensity();
            case SNOW:
                return getSnowGrassDensity();
            case DESERT:
                return getDesertGrassDensity();
        }
        return 1.0f;
    }

    public void setGrassDensity(WorldBiomeProvider.Biome biome, float density) {
        switch (biome) {
            case PLAINS:
                setPlainsGrassDensity(density);
                break;
            case MOUNTAINS:
                setMountainGrassDensity(density);
                break;
            case FOREST:
                setForestGrassDensity(density);
                break;
            case SNOW:
                setSnowGrassDensity(density);
                break;
            case DESERT:
                setDesertGrassDensity(density);
                break;
        }
    }

    public float getCoalDensity() {
        return coalDensity;
    }

    public void setCoalDensity(float coalDensity) {
        this.coalDensity = coalDensity;
    }

    public float getIronDensity() {
        return ironDensity;
    }

    public void setIronDensity(float ironDensity) {
        this.ironDensity = ironDensity;
    }

    public float getCopperDensity() {
        return copperDensity;
    }

    public void setCopperDensity(float copperDensity) {
        this.copperDensity = copperDensity;
    }

    public float getGoldDensity() {
        return goldDensity;
    }

    public void setGoldDensity(float goldDensity) {
        this.goldDensity = goldDensity;
    }

    public float getDiamondDensity() {
        return diamondDensity;
    }

    public void setDiamondDensity(float diamondDensity) {
        this.diamondDensity = diamondDensity;
    }

    public float getGravelDensity() {
        return gravelDensity;
    }

    public void setGravelDensity(float gravelDensity) {
        this.gravelDensity = gravelDensity;
    }
}
