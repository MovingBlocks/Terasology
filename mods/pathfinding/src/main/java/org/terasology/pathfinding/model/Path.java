package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;

import java.util.ArrayList;

/**
 * @author synopia
 */
public class Path extends ArrayList<WalkableBlock> {
    public static final Path INVALID = new Path();
}
