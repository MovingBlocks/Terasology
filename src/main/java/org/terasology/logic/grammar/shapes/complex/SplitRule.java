package org.terasology.logic.grammar.shapes.complex;

import org.terasology.logic.grammar.shapes.Shape;
import org.terasology.math.Matrix4i;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tobias 'Skaldarnar' Nett
 * @version 0.4
 *          <p/>
 *          A SplitRule creates new sub shapes using a specific split type. Implemented split types at the moment are "Walls" and "Inner".
 *          <p/>
 *          In the grammar, a split rule is of the form split { [type]  successor; } where _successor_ is a (complex) shape and _type_ a
 *          predefined split type.
 */
public class SplitRule extends ComplexRule {

    /** A list of the arguments of this SplitRule. Such an argument consists of a type and a shape. */
    List<SplitArg> args;

    //TODO: Strategy for SplitType

    /**
     * Constructs a new SplitRule with the given arguments.
     *
     * @param args
     */
    public SplitRule(List<SplitArg> args) {
        this.args = args;
    }

    /**
     * Constructs a new SplitRule with the given arguments and probability.
     *
     * @param args
     * @param probability
     */
    public SplitRule(List<SplitArg> args, float probability) {
        this(args);
        this.probability = probability;
    }

    @Override
    public List<Shape> getElements() {
        List<Shape> elements = new ArrayList<Shape>();

        for (SplitArg arg : args) {
            switch (arg.getType()) {
                case WALLS:
                    elements.addAll(getWallElements(arg.getShape()));
                    break;
                case INNER:
                    elements.add(getInnerElement(arg.getShape()));
                    break;
            }
        }
        return elements;
    }

    /**
     * Returns the inner part of a box (all walls, top and bottom are spared out).
     *
     * @param shape the shape applied to the _split_ rule
     *
     * @return the shape with proper position and dimension
     */
    private Shape getInnerElement(Shape shape) {
        // set sub-shape position according to the dimension
        shape.move(new Vector3i((dimension.x < 0) ? -1 : 1, (dimension.y < 0) ? -1 : 1, (dimension.z < 0) ? 1 : -1));
        // reduce sub-shape dimensions accordingly
        int _dx = (dimension.x > 0) ? dimension.x - 2 : dimension.x + 2;
        int _dy = (dimension.y > 0) ? dimension.y - 2 : dimension.y + 2;
        int _dz = (dimension.z > 0) ? dimension.z - 2 : dimension.z + 2;

        shape.setDimension(_dx, _dy, _dz);
        return shape;
    }

    /**
     * Returns a list of the four wall elements.
     * <p/>
     * Each wall's coordinate system is translated in a way that you look on the wall straight from the outside.
     *
     * @param shape the Shape that should be applied to this split
     *
     * @return list of the wall elements
     */
    private List<Shape> getWallElements(Shape shape) {
        // the original dimensions of the split rule
        int dx = dimension.x;
        int dy = dimension.y;
        int dz = dimension.z;

        // the list of shapes to return (the wall elements)
        List<Shape> elements = new ArrayList<Shape>();

        Matrix4i translation = Matrix4i.id();
        Matrix4i m = new Matrix4i();
        Vector3i d = new Vector3i();
        Shape s;

        // --------------------------------------------------------------
        // "front" wall
        // --------------------------------------------------------------
        s = shape.clone();
        s.setDimension(dx, dy, 1);
        s.setMatrix(matrix.clone());
        elements.add(s.clone());

        // --------------------------------------------------------------
        // "right" wall
        // --------------------------------------------------------------
        Shape r = shape.clone();
        //s = shape.clone();
        Matrix4i right = new Matrix4i(matrix);
        right.translate((dx > 0) ? dx - 1 : -(dx - 1), 0, 0);      // Translation for new position
        m.rotY((float) (Math.PI / 2f));       // apply rotation
        right.mul(m);
        //s.setMatrix(right);
        r.setMatrix(right);
        //s.setDimension(dz, dy, 1);
        r.setDimension(dz, dy, 1);
        //elements.add(s.clone());
        elements.add(r);

        // --------------------------------------------------------------
        // "back" wall
        // --------------------------------------------------------------
        s = shape.clone();
        Matrix4i back = new Matrix4i(matrix);
        back.translate((dx < 0) ? -(dx - 1) : dx - 1, 0, (dz > 0) ? -(dz - 1) : dz - 1);
        m.rotY((float) (Math.PI));      // apply rotation
        back.mul(m);
        s.setMatrix(back);
        s.setDimension(dx, dy, 1);
        elements.add(s.clone());

        // --------------------------------------------------------------
        // "left" wall
        // --------------------------------------------------------------
        s = shape.clone();
        Matrix4i left = new Matrix4i(matrix);
        left.translate(0, 0, (dz > 0) ? -(dz - 1) : dz - 1);
        m.rotY((float) (3 * Math.PI) / 2);
        left.mul(m);
        s.setMatrix(left);
        s.setDimension(dz, dy, 1);
        elements.add(s.clone());

        return elements;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("split ");
        builder.append(" { \n");
        for (SplitArg arg : args) {
            builder.append("\t ");
            builder.append(arg.toString());
            builder.append("\n");
        }
        builder.append("};");
        return builder.toString();
    }

    @Override
    public Shape clone() {
        SplitRule clone = new SplitRule(args, probability);
        clone.setActive(active);
        clone.setMatrix(matrix);
        clone.setDimension(dimension);
        return clone;
    }
}
