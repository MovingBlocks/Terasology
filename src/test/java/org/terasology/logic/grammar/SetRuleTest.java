package org.terasology.logic.grammar;

import org.junit.Before;
import org.junit.Test;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 29.08.12
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
 */
public class SetRuleTest {

    SetRule successor;
    ShapeSymbol scope;
    BlockCollection collection;
    BlockUri uri;

    @Before
    public void setUp() throws Exception {
        uri = new BlockUri("engine", "CobbleStone");
        scope = new ShapeSymbol("scope");

        scope.setDimension(2, 4, 6);

        successor = new SetRule(uri);
        successor.setDimension(2, 4, 6);

        collection = new BlockCollection();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 6; z++) {
                    collection.addBlock(new BlockPosition(x, y, z), BlockManager.getInstance().getBlock(uri));
                }
            }
        }
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("Set ( \"engine:cobblestone\" );", successor.toString());
    }

    @Test
    public void testGetElements() throws Exception {
        List<Shape> elements = successor.getElements();
        assertEquals(1, elements.size());
        Shape s = elements.get(0);
        assertTrue(s instanceof TerminalShape);
        TerminalShape t = (TerminalShape) s;
        assertEquals(collection, t.getValue());
    }
}
