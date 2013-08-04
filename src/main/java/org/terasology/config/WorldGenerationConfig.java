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

package org.terasology.config;

import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author Immortius
 */
public class WorldGenerationConfig {
    private String worldTitle = "New World";
    private float forestGrassDensity = 0.3f;
    private float plainsGrassDensity = 0.2f;
    private float snowGrassDensity = 0.001f;
    private float mountainGrassDensity = 0.2f;
    private float desertGrassDensity = 0.001f;
    private String defaultSeed = "Terasology";

    private float zoomFactor = 8f;
    private float baseTerrainFactor = 50.0f;
    private float oceanTerrainFactor = 50.0f;
    private float riverTerrainFactor = 50.0f;
    private float mountainFactor = 50.0f;
    private float hillDensityFactor = 50.0f;
    private float plateauAreaFactor = 50.0f;
    private float caveDensityFactor = 50.0f;

    private MapGeneratorUri defaultGenerator = new MapGeneratorUri("core:perlin");

    public String getWorldTitle() {
        return worldTitle;
    }

    public void setWorldTitle(String worldTitle) {
        this.worldTitle = worldTitle;
    }

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

    public String getDefaultSeed() {
        return defaultSeed;
    }

    public void setDefaultSeed(String defaultSeed) {
        this.defaultSeed = defaultSeed;
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(float zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public float getBaseTerrainFactor() {
        return baseTerrainFactor;
    }

    public void setBaseTerrainFactor(float baseTerrainFactor) {
        this.baseTerrainFactor = baseTerrainFactor;
    }

    public float getOceanTerrainFactor() {
        return oceanTerrainFactor;
    }

    public void setOceanTerrainFactor(float oceanTerrainFactor) {
        this.oceanTerrainFactor = oceanTerrainFactor;
    }

    public float getRiverTerrainFactor() {
        return riverTerrainFactor;
    }

    public void setRiverTerrainFactor(float riverTerrainFactor) {
        this.riverTerrainFactor = riverTerrainFactor;
    }

    public float getMountainFactor() {
        return mountainFactor;
    }

    public void setMountainFactor(float mountainFactor) {
        this.mountainFactor = mountainFactor;
    }

    public float getHillDensityFactor() {
        return hillDensityFactor;
    }

    public void setHillDensityFactor(float hillDensityFactor) {
        this.hillDensityFactor = hillDensityFactor;
    }

    public float getPlateauAreaFactor() {
        return plateauAreaFactor;
    }

    public void setPlateauAreaFactor(float plateauAreaFactor) {
        this.plateauAreaFactor = plateauAreaFactor;
    }

    public float getCaveDensityFactor() {
        return caveDensityFactor;
    }

    public void setCaveDensityFactor(float caveDensityFactor) {
        this.caveDensityFactor = caveDensityFactor;
    }

    public MapGeneratorUri getDefaultGenerator() {
        return defaultGenerator;
    }

    public void setDefaultGenerator(MapGeneratorUri defaultGenerator) {
        this.defaultGenerator = defaultGenerator;
    }
}
