package org.terasology.grammarSystem.logic;


import org.junit.Before;
import org.junit.Test;
import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.ShapeSymbol;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.DivideArg;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.DivideRule;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.Size;
import org.terasology.math.Matrix4i;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/** @author Tobias 'skaldarnar' Nett */
public class DivideRuleTest {

    private static float heightY1 = 3f;
    private static float heightY2 = 4f;
    private static float heightZ1 = 1f;
    private static float heightZ2 = 2f;
    private static float height3 = 1f;
    private static float relHeight = .5f;
    private static final Vector3i posDim = new Vector3i(2, 7, 3);
    private static final Vector3i negYDim = new Vector3i(2, -7, 3);
    private static final Vector3i negZDim = new Vector3i(2, 7, -3);
    private static final Vector3i relDim = new Vector3i(5, 1, 1);

    private DivideRule verticalDivPositiveY, verticalDivNegativeY, horizontalDivZ, horizontalDivNegZ;
    private DivideRule relDivide;
    private Shape succ1, succ2;
    private Shape relSucc1, relSucc2, relSucc3;

    @Before
    public void setUp() throws Exception {
        succ1 = new ShapeSymbol("succ1");
        succ2 = new ShapeSymbol("succ2");

        //============================================================================//
        // dividing along Y axis (vertical)                                           //
        //============================================================================//
        DivideArg argY1 = new DivideArg(new Size(heightY1, true), succ1);
        DivideArg argY2 = new DivideArg(new Size(heightY2, true), succ2);
        List<DivideArg> argsY = new ArrayList<DivideArg>();
        argsY.add(argY1);
        argsY.add(argY2);

        verticalDivPositiveY = new DivideRule(argsY, DivideRule.Direction.Y);
        verticalDivPositiveY.setMatrix(Matrix4i.id());
        verticalDivPositiveY.setDimension(posDim);

        verticalDivNegativeY = new DivideRule(argsY, DivideRule.Direction.Y);
        verticalDivNegativeY.setMatrix(Matrix4i.id());
        verticalDivNegativeY.setDimension(negYDim);

        //============================================================================//
        // dividing along z axis (horizontal)                                         //
        //============================================================================//
        DivideArg argZ1 = new DivideArg(new Size(heightZ1, true), succ1);
        DivideArg argZ2 = new DivideArg(new Size(heightZ2, true), succ2);
        List<DivideArg> argsZ = new ArrayList<DivideArg>();
        argsZ.add(argZ1);
        argsZ.add(argZ2);

        horizontalDivNegZ = new DivideRule(argsZ, DivideRule.Direction.Z);
        horizontalDivNegZ.setMatrix(Matrix4i.id());
        horizontalDivNegZ.setDimension(negZDim);

        horizontalDivZ = new DivideRule(argsZ, DivideRule.Direction.Z);
        horizontalDivZ.setMatrix(Matrix4i.id());
        horizontalDivZ.setDimension(posDim);

        //============================================================================//
        // dividing with relative sizes                                                 //
        //============================================================================//
        relSucc1 = new ShapeSymbol("relSucc1");
        relSucc2 = new ShapeSymbol("relSucc2");
        relSucc3 = new ShapeSymbol("relSucc3");
        DivideArg relArg1 = new DivideArg(new Size(relHeight, false), relSucc1);
        DivideArg relArg2 = new DivideArg(new Size(height3, true), relSucc2);
        DivideArg relArg3 = new DivideArg(new Size(relHeight, false), relSucc3);
        List<DivideArg> relArgs = new ArrayList<DivideArg>();
        relArgs.add(relArg1);
        relArgs.add(relArg2);
        relArgs.add(relArg3);
        relDivide = new DivideRule(relArgs, DivideRule.Direction.X);
        relDivide.setMatrix(Matrix4i.id());
        relDivide.setDimension(relDim);
    }

    @Test
    public void testPositiveYDimension() throws Exception {
        Shape reference1 = succ1;
        reference1.setDimension(2, (int) heightY1, 3);

        Shape reference2 = succ2;
        reference2.setDimension(2, (int) heightY2, 3);
        reference2.setPosition(0, 3, 0);

        assertTrue(verticalDivPositiveY.getElements().contains(reference1));
        assertTrue(verticalDivPositiveY.getElements().contains(reference2));
        assertEquals(2, verticalDivPositiveY.getElements().size());
        assertEquals("Wrong translation vector for reference2!", new Vector3i(0, 3, 0)
                , verticalDivPositiveY.getElements().get(verticalDivPositiveY.getElements().indexOf(reference2)).getMatrix().getTranslation());
        assertEquals("Wrong translation vector for reference1!", new Vector3i(0, 0, 0)
                , verticalDivPositiveY.getElements().get(verticalDivPositiveY.getElements().indexOf(reference1)).getMatrix().getTranslation());
    }

    @Test
    public void testNegativeYDimension() throws Exception {
        Shape expected1 = succ1;
        expected1.setDimension(2, -(int) heightY1, 3);

        Shape expected2 = succ2;
        expected2.setDimension(2, -(int) heightY2, 3);
        expected2.setPosition(0, -3, 0);

        assertTrue("First successor must be part of the rules elements!", verticalDivNegativeY.getElements().contains
                (expected1));
        assertTrue("Second successor must be contained in the elements!", verticalDivNegativeY.getElements().contains
                (expected2));
        assertEquals("The must be only two successor shapes!", 2, verticalDivNegativeY.getElements().size());

        assertEquals("Wrong translation/position for successor 1!", new Vector3i(0, 0, 0),
                verticalDivNegativeY.getElements().get(verticalDivNegativeY.getElements().indexOf(expected1)).getMatrix().getTranslation());
        assertEquals("Wrong translation/position for successor 2!", new Vector3i(0, -3, 0),
                verticalDivNegativeY.getElements().get(verticalDivNegativeY.getElements().indexOf(expected2)).getMatrix().getTranslation());
    }

    @Test
    public void testNegativeZDimension() throws Exception {
        Shape expected1 = succ1;
        expected1.setDimension(2, 7, -(int) heightZ1);

        Shape expected2 = succ2;
        expected2.setDimension(2, 7, -(int) heightZ2);
        expected2.setPosition(0, 0, 1);

        List<Shape> elements = horizontalDivNegZ.getElements();

        assertTrue("First successor must be part of the rules elements!", elements.contains(expected1));
        assertTrue("Second successor must be contained in the elements!", elements.contains(expected2));
        assertEquals("The must be only two successor shapes!", 2, elements.size());

        assertEquals("Wrong translation/position for successor 1!", new Vector3i(0, 0, 0),
                elements.get(elements.indexOf(expected1)).getMatrix().getTranslation());
        assertEquals("Wrong translation/position for successor 2!", new Vector3i(0, 0, 1),
                elements.get(elements.indexOf(expected2)).getMatrix().getTranslation());
    }

    @Test
    public void testPositiveDimensionsZ() throws Exception {
        Shape expected1 = succ1;
        expected1.setDimension(2, 7, (int) heightZ1);

        Shape expected2 = succ2;
        expected2.setDimension(2, 7, (int) heightZ2);
        expected2.setPosition(0, 0, -1);

        List<Shape> elements = horizontalDivZ.getElements();

        assertTrue("First successor must be part of the rules elements!", elements.contains(expected1));
        assertTrue("Second successor must be contained in the elements!", elements.contains
                (expected2));
        assertEquals("The must be only two successor shapes!", 2, elements.size());

        assertEquals("Wrong translation/position for successor 1!", new Vector3i(0, 0, 0),
                elements.get(elements.indexOf(expected1)).getMatrix().getTranslation());
        assertEquals("Wrong translation/position for successor 2!", new Vector3i(0, 0, -1),
                elements.get(elements.indexOf(expected2)).getMatrix().getTranslation());
    }

    @Test
    public void testRelativeSize() throws Exception {
        Shape expected1 = relSucc1;
        expected1.setDimension(2, 1, 1);

        Shape expected2 = relSucc2;
        expected2.setDimension(1, 1, 1);
        expected2.setPosition(2, 0, 0);

        Shape expected3 = relSucc3;
        expected3.setDimension(2, 1, 1);
        expected3.setPosition(3, 0, 0);

        List<Shape> elements = relDivide.getElements();
        assertTrue("First element must part of the result!", elements.contains(expected1));
        assertTrue("Second element must part of the result!", elements.contains(expected2));
        assertTrue("Third element must part of the result!", elements.contains(expected3));

        assertEquals("Wrong translation/position for successor 1!", new Vector3i(0, 0, 0), elements.get(elements.indexOf(expected1))
                .getMatrix().getTranslation());
        assertEquals("Wrong translation/position for successor 2!", new Vector3i(2, 0, 0), elements.get(elements.indexOf(expected2))
                .getMatrix().getTranslation());
        assertEquals("Wrong translation/position for successor 3!", new Vector3i(3, 0, 0), elements.get(elements.indexOf(expected3))
                .getMatrix().getTranslation());
    }
}
