package org.terasology.logic.grammar;

import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 30.08.12
 * Time: 02:00
 * To change this template use File | Settings | File Templates.
 */
public class SplitRule extends ComplexRule {

    List<SplitArg> args;

    public SplitRule(List<SplitArg> args) {
        this.args = args;
    }

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
     * @return the shape with proper position and dimension
     */
    private Shape getInnerElement(Shape shape) {
        shape.setPosition(new Vector3i(position.x + 1, position.y + 1, position.z + 1));
        shape.setDimension(dimension.x - 2, dimension.y - 2, dimension.z - 2);
        shape.setCoordinateSystem(coordinateSystem);
        return shape;
    }

    /**
     * Returns a list of the four wall elements.
     * <p/>
     * Each wall's coordinate system is translated in a way that you look on the wall straight from the outside.
     *
     * @param shape the Shape that should be applied to this split
     * @return list of the wall elements
     */
    private List<Shape> getWallElements(Shape shape) {
        // the origin of the bounding box
        int px = position.x;
        int py = position.y;
        int pz = position.z;
        // the original dimensions of the _split_ command
        int dx = dimension.x;
        int dy = dimension.y;
        int dz = dimension.z;
        // the original coordinate system
        Vector3i x = coordinateSystem.getX();
        Vector3i y = coordinateSystem.getY();
        Vector3i z = coordinateSystem.getZ();

        Vector3i minusX = x.clone();
        minusX.mult(-1);
        Vector3i minusZ = z.clone();
        z.mult(-1);

        // the list of shapes to return (the 4 walls)
        List<Shape> elements = new ArrayList<Shape>();

        // front wall
        shape.setCoordinateSystem(coordinateSystem);
        shape.setPosition(position);
        shape.setDimension(dx, dy, 1);
        elements.add(shape);

        // right wall
        Vector3i rightPos = new Vector3i(dx - 1, 0, 0);

        CoordinateSystem rightSystem = new CoordinateSystem(z, y, minusX);

        shape.setCoordinateSystem(rightSystem);
        shape.setPosition(rightPos);
        shape.setDimension(dz, dy, 1);
        elements.add(shape);

        // back wall
        Vector3i backPos = new Vector3i(dx - 1, 0, dy - 1);


        shape.setCoordinateSystem(new CoordinateSystem(minusX, y, minusZ));
        shape.setDimension(dx, dy, 1);
        shape.setPosition(backPos);
        elements.add(shape);

        // left wall
        Vector3i leftPos = new Vector3i(0, 0, dy - 1);

        shape.setCoordinateSystem(new CoordinateSystem(minusZ, y, x));
        shape.setPosition(leftPos);
        shape.setDimension(dz, dy, 1);
        elements.add(shape);

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
}
