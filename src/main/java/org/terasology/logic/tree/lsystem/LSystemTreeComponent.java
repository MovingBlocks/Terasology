package org.terasology.logic.tree.lsystem;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.ForceBlockActive;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public class LSystemTreeComponent implements Component {
    public String axion;
    public int generation;
    public long lastGrowthTime;
    public float branchAngle;
    public float rotationAngle;

    public boolean initialized = false;
}
