package org.terasology.config;


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
    private float hillsGrassDensity = 0.25f;

    private String defaultSeed = "sGfJeVcSeYxDwArXwDlOfUaTnGgRfGzA";
    private float ZOOM_FACTOR = 8f;
    private float  BaseTerrainFACTOR = 50.0f;
    private float  OceanTerrainFACTOR = 50.0f;
    private float  RiverTerrainFACTOR = 50.0f;
    private float  MountainFACTOR = 50.0f;
    private float  HillDensityFACTOR = 50.0f;
    private float  plateauAreaFACTOR = 50.0f;
    private float  caveDensityFACTOR = 50.0f;

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
    public float getHillsGrassDensity() {
        return hillsGrassDensity;
    }
    public void setHillsGrassDensity(float hillsGrassDensity)
    {
        this.hillsGrassDensity = hillsGrassDensity;
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

    public float getZOOM_FACTOR() {
        return ZOOM_FACTOR;
    }

    public void setZOOM_FACTOR(float ZOOM_FACTOR) {
        this.ZOOM_FACTOR = ZOOM_FACTOR;
    }

    public float getBaseTerrainFACTOR() {
		return BaseTerrainFACTOR;
    }

    public void setBaseTerrainFACTOR(float BaseTerrainFACTOR) {
        this.BaseTerrainFACTOR = BaseTerrainFACTOR;}

    public float  getOceanTerrainFACTOR() {
        return OceanTerrainFACTOR;
    }

    public void setOceanTerrainFACTOR(float OceanTerrainFACTOR) {
        this.OceanTerrainFACTOR = OceanTerrainFACTOR;
    }

    public float  getRiverTerrainFACTOR() {
        return RiverTerrainFACTOR;
    }

    public void setRiverTerrainFACTOR(float RiverTerrainFACTOR) {
        this.RiverTerrainFACTOR = RiverTerrainFACTOR;}

    public float  getMountainFACTOR() {
        return MountainFACTOR;
    }

    public void setMountainFACTOR(float MountainFACTOR) {
        this.MountainFACTOR = MountainFACTOR;
    }

    public float getHillDensityFACTOR() {
        return HillDensityFACTOR;
    }

    public void setHillDensityFACTOR(float  HillDensityFACTOR) {
        this.HillDensityFACTOR = HillDensityFACTOR;
    }

    public float  getplateauAreaFACTOR() {
        return plateauAreaFACTOR;
    }

    public void setplateauAreaFACTOR(float  plateauAreaFACTOR) {
        this.plateauAreaFACTOR = plateauAreaFACTOR;
    }

    public float  getcaveDensityFACTOR() {
        return caveDensityFACTOR;
    }

    public void setcaveDensityFACTOR(float  caveDensityFACTOR) {
        this.caveDensityFACTOR = caveDensityFACTOR;
    }

}
