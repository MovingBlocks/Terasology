package org.terasology.logic.grammar;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public class DivideRuleTest {

    float height1 = 3f;
    float height2 = 4f;

    DivideRule vertDiv;
    List<DivideArg> args;
    DivideArg arg1, arg2;
    Shape succ1, succ2;

    @Before
    public void setUp() throws Exception {
        succ1 = new ShapeSymbol("succ1");
        succ2 = new ShapeSymbol("succ2");
        arg1 = new DivideArg(new Size(height1, true), succ1);
        arg2 = new DivideArg(new Size(height2, true), succ2);
        args = new ArrayList<DivideArg>();
        args.add(arg1);
        args.add(arg2);
        vertDiv = new DivideRule(args, DivideRule.Direction.Y);

        vertDiv.setDimension(2, 7, 3);
    }

    @Test
    public void testGetElements() throws Exception {
        Shape ref1 = succ1;
        ref1.setDimension(2, (int) height1, 3);

        Shape ref2 = succ2;
        ref2.setDimension(2, (int) height2, 3);
        ref2.setPosition(new Vector3i(0, 3, 0));

        assertTrue(vertDiv.getElements().contains(ref1));
        assertTrue(vertDiv.getElements().contains(ref2));
        assertEquals(2, vertDiv.getElements().size());

    }
}
