package org.terasology.logic.grammar;

import org.terasology.math.Vector3i;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public abstract class Shape {

    /**
     * The relative position of this shape.
     */
    protected Vector3i position = Vector3i.zero();
    /**
     * The shape's dimension along its X,Y and Z axis.
     */
    protected Vector3i dimension = Vector3i.zero();
    /**
     * The local coordinate system.
     */
    protected CoordinateSystem coordinateSystem = CoordinateSystem.cartesianSystem();
    /**
     * The probability that this shape appears in an derivation.
     */
    protected float probability = 1f;
    /**
     * Indicates if the shape is active.
     */
    protected boolean active = true;

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(Vector3i position) {
        this.position = position;
    }

    public int getWidth() {
        return dimension.x;
    }

    public int getHeight() {
        return dimension.y;
    }

    public int getDepth() {
        return dimension.z;
    }

    public Vector3i getDimension() {
        return dimension;
    }

    public void setDimension(Vector3i dimension) {
        this.dimension = dimension;
    }

    public void setDimension(int x, int y, int z) {
        dimension.x = x;
        dimension.y = y;
        dimension.z = z;
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * The probability for the successor. A float value between 0 and 1.
     *
     * @return the probability of this successor.
     */
    public float getProbability() {
        return probability;
    }
}
