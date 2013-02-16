package org.terasology.config;

import com.google.common.collect.Lists;

import java.util.List;

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
    private String defaultSeed = "Blockmaina42";

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
}
