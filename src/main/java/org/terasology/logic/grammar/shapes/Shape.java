package org.terasology.logic.grammar.shapes;

import org.terasology.math.Matrix4i;
import org.terasology.math.Vector3i;

/**
 * @author Tobias 'skaldarnar' Nett <p/> A shape is the basis for structure generation. </p> Shapes have a dimension and a relative
 *         position. The shape's rotation is stored in its coordinate system. Moreover, every shaps has a probability (default 1) and a flag
 *         that indicates if the shape is active or not.
 */
public abstract class Shape {

    /**
     * The relative position of this shape.
     */
    //protected Vector3i position = Vector3i.zero();
    /** The shape's dimension along its X,Y and Z axis. */
    protected Vector3i dimension = Vector3i.zero();

    /** The probability that this shape appears in an derivation. */
    protected float probability = 1f;
    /** Indicates if the shape is active. */
    protected boolean active = true;
    /** Transformation matrix - rotation & translation */
    //protected Matrix4f matrix = new Matrix4f();
    protected Matrix4i matrix = Matrix4i.id();

    /**
     * Returns the shape's relative position
     *
     * @return the shape's relative position
     */
    public Vector3i getPosition() {
        //return position;
        return matrix.getTranslation();
    }

    /**
     * Sets the shape's position.
     *
     * @param position the new position
     */
    public void setPosition(Vector3i position) {
        matrix.setTranslation(position);
    }

    public void setPosition(int x, int y, int z) {
        matrix.setTranslation(x, y, z);
    }

    /**
     * The shape's width. The width is calculated along the shapes internal x axis.
     *
     * @return the width
     */
    public int getWidth() {
        return dimension.x;
    }

    /**
     * The shape's height. The height is calculated along the shape's internal y axis.
     *
     * @return the height
     */
    public int getHeight() {
        return dimension.y;
    }

    /**
     * The shape's depth. The depth is calculated along the shape's internal z axis.
     *
     * @return the depth
     */
    public int getDepth() {
        return dimension.z;
    }

    /**
     * The shape's dimension as a 3 dimensional vector indicating (width, height, depth) along the axis (x,y,z).
     *
     * @return the dimension
     */
    public Vector3i getDimension() {
        return dimension;
    }

    /**
     * Sets the dimension to the sizes specified by {@code dimension} along the axis (x,y,z).
     *
     * @param dimension the new dimension
     */
    public void setDimension(Vector3i dimension) {
        this.dimension = dimension;
    }

    /**
     * Sets the dimension to the sizes specified by the arguments for x (width), y (height) and z (depth).
     *
     * @param x the new width
     * @param y the new height
     * @param z the new depth
     */
    public void setDimension(int x, int y, int z) {
        dimension.x = x;
        dimension.y = y;
        dimension.z = z;
    }

    /**
     * Indicates whether the shape is active or not. </p> This is only relevant for the derivation tree.
     *
     * @return true if the shape is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active flag of this shape. If the shape is not active, it won't be taken into consideration for further derivation steps.
     *
     * @param active the new active flag - false if the shape should be inactive
     */
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

    public Matrix4i getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix4i matrix) {
        this.matrix = matrix;
    }

    public void move(Vector3i translation) {
        matrix = matrix.translate(translation);
    }

    public abstract Shape clone();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shape)) return false;

        Shape shape = (Shape) o;

        if (active != shape.active) return false;
        if (Float.compare(shape.probability, probability) != 0) return false;
        if (dimension != null ? !dimension.equals(shape.dimension) : shape.dimension != null) return false;
        if (matrix != null ? !matrix.equals(shape.matrix) : shape.matrix != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dimension != null ? dimension.hashCode() : 0;
        result = 31 * result + (probability != +0.0f ? Float.floatToIntBits(probability) : 0);
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (matrix != null ? matrix.hashCode() : 0);
        return result;
    }
}
