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
//MPratt "Map Gen Setup" start
//1. BaseTerrainFACTOR, 2. OceanTerrainFACTOR, 3. RiverTerrainFACTOR, 4. MountainFACTOR,
// 5. HillDensityFACTOR, 6. plateauAreaFACTOR, 7. caveDensityFACTOR;
    private String BaseTerrainFACTOR="100";
    private String OceanTerrainFACTOR="100";
    private String RiverTerrainFACTOR="100";
    private String MountainFACTOR="100";
    private String HillDensityFACTOR="100";
    private String plateauAreaFACTOR="100";
    private String caveDensityFACTOR="100";
//MPratt "Map Gen Setup" end

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

//MPratt "Map Gen Setup" start
//1. BaseTerrainFACTOR, 2. OceanTerrainFACTOR, 3. RiverTerrainFACTOR, 4. MountainFACTOR,
// 5. HillDensityFACTOR, 6. plateauAreaFACTOR, 7. caveDensityFACTOR;
    public String getBaseTerrainFACTOR() {
    return BaseTerrainFACTOR;
    }

    public void setBaseTerrainFACTOR(String BaseTerrainFACTOR) {
        this.BaseTerrainFACTOR = BaseTerrainFACTOR;}

    public String getOceanTerrainFACTOR() {
        return OceanTerrainFACTOR;
    }

    public void setOceanTerrainFACTOR(String OceanTerrainFACTOR) {
        this.OceanTerrainFACTOR = OceanTerrainFACTOR;
    }

    public String getRiverTerrainFACTOR() {
        return RiverTerrainFACTOR;
    }

    public void setRiverTerrainFACTOR(String RiverTerrainFACTOR) {
        this.RiverTerrainFACTOR = RiverTerrainFACTOR;}

    public String getMountainFACTOR() {
        return MountainFACTOR;
    }

    public void setMountainFACTOR(String MountainFACTOR) {
        this.MountainFACTOR = MountainFACTOR;
    }

    public String getHillDensityFACTOR() {
        return HillDensityFACTOR;
    }

    public void setHillDensityFACTOR(String HillDensityFACTOR) {
        this.HillDensityFACTOR = HillDensityFACTOR;
    }

    public String getplateauAreaFACTOR() {
        return plateauAreaFACTOR;
    }

    public void setplateauAreaFACTOR(String plateauAreaFACTOR) {
        this.plateauAreaFACTOR = plateauAreaFACTOR;
    }

    public String getcaveDensityFACTOR() {
        return caveDensityFACTOR;
    }

    public void setcaveDensityFACTOR(String caveDensityFACTOR) {
        this.caveDensityFACTOR = caveDensityFACTOR;
    }

//MPratt "Map Gen Setup" end
}
