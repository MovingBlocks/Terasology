package org.terasology.logic.grammar;

import org.terasology.math.Vector3i;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 31.08.12
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class CoordinateSystem {

    private Vector3i x = Vector3i.unitX();
    private Vector3i y = Vector3i.unitY();
    private Vector3i z = Vector3i.unitZ();

    public CoordinateSystem(Vector3i x, Vector3i y, Vector3i z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static CoordinateSystem cartesianSystem() {
        return new CoordinateSystem(Vector3i.unitX(), Vector3i.unitY(), Vector3i.unitZ());
    }

    public Vector3i getX() {
        return x;
    }

    public Vector3i getY() {
        return y;
    }

    public Vector3i getZ() {
        return z;
    }
}
