package org.terasology.logic.grammar.shapes.complex;

import org.terasology.logic.grammar.shapes.Shape;
import org.terasology.math.Matrix4i;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

/**
 * The DivideRule is a complex shape rule that describes the division of its bounding box.
 * <p/>
 * The successor elements are calculated based on the geometric attributes of this shape.
 *
 * @author Tobias 'skaldarnar' Nett
 */
public class DivideRule extends ComplexRule {

    /** The list of arguments of the _divide_ command. */
    private List<DivideArg> args;
    /** The direction of the _divide_ command. */
    private Direction direction;

    @Override
    public Shape clone() {
        DivideRule clone = new DivideRule(args, direction, probability);
        clone.setActive(active);
        clone.setMatrix(matrix);
        clone.setDimension(dimension);
        return clone;
    }

    /** Defines the possible directions of a _divide_ command. */
    public enum Direction {
        X, Y, Z
    }

    public DivideRule(List<DivideArg> args, Direction direction, float probability) {
        this(args, direction);
        this.probability = probability;
    }

    public DivideRule(List<DivideArg> args, Direction direction) {
        this.args = args;
        this.direction = direction;
    }

    /**
     * This method returns the successor elements of the _divide_ command this object is representing.
     * <p/>
     * The successor elements depend on the specific command and its direction, e.g. the following command would have 2 successor elements,
     * 'successor_1' and 'successor_2'. The scope (bounding box) of the complex divide shape rule would be divided into 2 parts with the
     * ratio of 0.7 to 0.3 along the Y axis (vertical division).
     * <p/>
     * divide y { [70%] successor_1 [30%] successor_2 }
     *
     * @return the successor elements the _divide_ command
     */
    public List<Shape> getElements() {
        // mutable variable for the new subdivisions dimension
        // Calculate the remaining size for relative values. (depending on the direction)
        int remainingSize = 0;
        switch (direction) {
            case X:
                remainingSize = Math.abs(dimension.x);
                break;
            case Y:
                remainingSize = Math.abs(dimension.y);
                break;
            case Z:
                remainingSize = Math.abs(dimension.z);
                break;
        }
        for (DivideArg arg : args) {
            if (arg.getSize().isAbsolute()) {
                remainingSize -= arg.getSize().getValue();
            }
        }
        // Preparing the return list.
        List<Shape> elements = new ArrayList<Shape>(args.size());
        //Vector3i newPos = position;
        int newSize;
        float divideFactor = 1;
        Matrix4i m = new Matrix4i(matrix);

        Vector3i translation = new Vector3i();
        for (DivideArg arg : args) {

            Shape s = arg.getShape().clone();
            // Calculate the size of the new sub shape.
            if (arg.getSize().isAbsolute()) {
                newSize = (int) arg.getSize().getValue();
            } else {
                float factor = arg.getSize().getValue() / divideFactor;
                newSize = (int) (remainingSize * factor + 0.49f);
                divideFactor *= (1 - arg.getSize().getValue());
                remainingSize -= newSize;
            }
            // Set _newPos_ to the next 'free' position and specify the new shape's dimension
            // (the value will only change for _direction_).
            switch (direction) {
                case X:
                    s.setDimension((dimension.x < 0) ? -newSize : newSize, dimension.y, dimension.z);
                    //m.translate(newSize, 0, 0);
                    m.translate(translation);
                    m.transform(new Vector3i((dimension.x < 0) ? -newSize : newSize, 0, 0), translation);
                    //m.setTranslation(translation);
                    //translation.add((dimension.x < 0) ? -newSize : newSize, 0, 0);
                    //translation.set((dimension.x < 0) ? -newSize : newSize, 0, 0);
                    break;
                case Y:
                    s.setDimension(dimension.x, (dimension.y < 0) ? -newSize : newSize, dimension.z);
                    m.translate(translation);
                    //translation.add(0, (dimension.y < 0) ? -newSize : newSize, 0);
                    m.transform(new Vector3i(0, (dimension.y < 0) ? -newSize : newSize, 0), translation);
                    //m.translate(0, newSize, 0);
                    break;
                case Z:
                    s.setDimension(dimension.x, dimension.y, (dimension.z < 0) ? -newSize : newSize);
                    m.translate(translation);
                    //translation.add(0, 0, (dimension.z < 0) ? newSize : -newSize);
                    m.transform(new Vector3i(0, 0, (dimension.z < 0) ? newSize : -newSize), translation);
                    //m.translate(0, 0, newSize);
                    break;
            }
            s.setMatrix(m.clone());
            // …and add the newly created shape symbol to the successor elements list.
            elements.add(s.clone());
        }
        return elements;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("divide ");
        builder.append(direction.name());
        builder.append(" { \n");
        for (DivideArg arg : args) {
            builder.append("\t ");
            builder.append(arg.toString());
            builder.append("\n");
        }
        builder.append("};");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DivideRule)) return false;
        if (!super.equals(o)) return false;

        DivideRule that = (DivideRule) o;

        if (args != null ? !args.equals(that.args) : that.args != null) return false;
        if (direction != that.direction) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (args != null ? args.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }
}
