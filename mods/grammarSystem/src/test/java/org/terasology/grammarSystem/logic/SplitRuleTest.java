package org.terasology.grammarSystem.logic;

import org.junit.Before;
import org.junit.Test;
import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.ShapeSymbol;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.SplitArg;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.SplitRule;
import org.terasology.math.Matrix4i;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/** @author Tobias 'skaldarnar' Nett Date: 15.12.12 */
public class SplitRuleTest {

    private static final Vector3i positiveDimension = new Vector3i(3, 2, 4);
    private static final Vector3i testInnerDimension = new Vector3i(4, 3, 5);
    private static final Vector3i testInnerNegDimension = new Vector3i(-4, 3, -5);

    private static final String shapeName = "shape";

    private SplitRule splitInner, splitInnerNeg, splitWalls;
    private Shape shape;

    private Shape front, right, back, left;

    @Before
    public void setUp() throws Exception {

        shape = new ShapeSymbol(shapeName);
        shape.setMatrix(Matrix4i.id());

        setUpInnerTest();

        setUpWallTest();
    }

    /**
     * Set split rule to:
     * <pre>
     * split (
     * [WALLS] shape
     * );
     * </pre>
     */
    private void setUpWallTest() {
        List<SplitArg> args = new ArrayList<SplitArg>();
        args.add(new SplitArg(SplitArg.SplitType.WALLS, shape));

        splitWalls = new SplitRule(args);
        splitWalls.setMatrix(Matrix4i.id());
        splitWalls.setDimension(positiveDimension);

        // The test elements --> the four wall elements:
        //---------------------------------
        // Front
        //---------------------------------
        front = shape.clone();
        front.setMatrix(Matrix4i.id());
        front.setDimension(positiveDimension.x, positiveDimension.y, 1);
        //---------------------------------
        // Right
        //---------------------------------
        right = shape.clone();
        right.setMatrix(new Matrix4i(new int[]{0, 0, 1, 2, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 1}));
        right.setDimension(4, 2, 1);
        //---------------------------------
        // Back
        //---------------------------------
        back = shape.clone();
        back.setMatrix(new Matrix4i(new int[]{-1, 0, 0, 2, 0, 1, 0, 0, 0, 0, -1, -3, 0, 0, 0, 1}));
        //back.setDimension(-3, 2, 1);
        back.setDimension(3, 2, 1);
        //---------------------------------
        // Left
        //---------------------------------
        left = shape.clone();
        left.setMatrix(new Matrix4i(new int[]{0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, -3, 0, 0, 0, 1}));
        //left.setDimension(-4, 2, 1);
        left.setDimension(4, 2, 1);
    }

    /**
     * Set the split rule to:
     * <pre>
     * split (
     * [INNER] shape
     * );
     * </pre>
     */
    private void setUpInnerTest() {
        List<SplitArg> args = new ArrayList<SplitArg>();
        args.add(new SplitArg(SplitArg.SplitType.INNER, shape));
        splitInner = new SplitRule(args);
        splitInner.setDimension(testInnerDimension);
        splitInner.setMatrix(Matrix4i.id());

        splitInnerNeg = new SplitRule(args);
        splitInnerNeg.setDimension(testInnerNegDimension);
        splitInner.setMatrix(Matrix4i.id());
    }

    @Test
    public void testGetElements() throws Exception {
        List<Shape> elements = splitWalls.getElements();

        assertTrue("Cannot find element 'front'", elements.contains(front));

        assertTrue("Cannot find element 'left'", elements.contains(left));

        assertTrue("Cannot find element 'back'", elements.contains(back));

        assertTrue("Cannot find element 'right'", elements.contains(right));
    }

    @Test
    public void testInnerElement() throws Exception {
        List<Shape> result = splitInner.getElements();

        assertEquals("The rule should only result in one successor!", 1, result.size());
        assertEquals(new Vector3i(1, 1, -1), result.get(0).getPosition());
        assertEquals(shape, result.get(0));
        assertEquals(new Vector3i(2, 1, 3), result.get(0).getDimension());
    }

    @Test
    public void testInnerElementNegativeDimension() throws Exception {
        List<Shape> result = splitInnerNeg.getElements();

        assertEquals("The rule should only result in one successor!", 1, result.size());
        assertEquals(new Vector3i(-1, 1, 1), result.get(0).getPosition());
        assertEquals(shape, result.get(0));
        assertEquals(new Vector3i(-2, 1, -3), result.get(0).getDimension());
    }
}