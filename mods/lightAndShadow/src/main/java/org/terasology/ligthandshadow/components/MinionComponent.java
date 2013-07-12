package org.terasology.ligthandshadow.components;

import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.model.Path;

/**
 * @author synopia
 */
public class MinionComponent implements Component {
    public String side;
    public int healthTotal;
    /**
     * if set to a value other then null, this minion is requested to move to this position
     * once reached, targetBlock is set to null
     */
    public Vector3i targetBlock;

    public Path path;
    public boolean receivedNewPath;
    public int pathStep;
    public boolean dead;
}
