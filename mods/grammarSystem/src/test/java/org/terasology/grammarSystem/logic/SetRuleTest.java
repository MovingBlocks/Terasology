package org.terasology.grammarSystem.logic;

import org.junit.Before;
import org.junit.Test;
import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.TerminalShape;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.SetRule;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/** Created with IntelliJ IDEA. User: tobias Date: 29.08.12 Time: 18:04 To change this template use File | Settings | File Templates. */
public class SetRuleTest {

    private static final Vector3i position = new Vector3i(1, 1, 1);
    private static final Vector3i dimension = new Vector3i(2, 3, 4);

    private static final Vector3i negDimension = new Vector3i(2, 3, -4);

    private SetRule setRule, negSetRule;
    private BlockCollection collection, negCollection;

    @Before
    public void setUp() throws Exception {
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("some:uri"), new Block()));
        //Block block = BlockManager.getInstance().getBlock("some:uri");
        BlockUri uri = new BlockUri("some:uri");

        setRule = new SetRule(uri);
        setRule.setPosition(position);
        setRule.setDimension(dimension);

        negSetRule = new SetRule(uri);
        negSetRule.setPosition(position);
        negSetRule.setDimension(negDimension);

        collection = new BlockCollection();
        for (int x = position.x; x < position.x + dimension.x; x++) {
            for (int y = position.y; y < position.y + dimension.y; y++) {
                for (int z = position.z; z > position.z - dimension.z; z--) {
                    collection.addBlock(new BlockPosition(x, y, z), BlockManager.getInstance().getBlock(uri));
                }
            }
        }

        negCollection = new BlockCollection();
        for (int x = position.x; x < position.x + negDimension.x; x++) {
            for (int y = position.y; y < position.y + negDimension.y; y++) {
                for (int z = position.z; z < position.z + Math.abs(negDimension.z); z++) {
                    negCollection.addBlock(new BlockPosition(x, y, z), BlockManager.getInstance().getBlock(uri));
                }
            }
        }
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("Set ( \"some:uri\" );", setRule.toString());
    }

    @Test
    public void testSetRule() throws Exception {
        List<Shape> elements = setRule.getElements();
        assertEquals(1, elements.size());

        Shape s = elements.get(0);
        assertTrue(s instanceof TerminalShape);

        TerminalShape t = (TerminalShape) s;
        assertEquals(collection, t.getValue());
    }

    @Test
    public void testNegativeDimension() throws Exception {
        List<Shape> elements = negSetRule.getElements();
        assertEquals(1, elements.size());

        Shape s = elements.get(0);
        assertTrue(s instanceof TerminalShape);

        TerminalShape t = (TerminalShape) s;
        assertEquals(24, t.getValue().getBlocks().size());
        assertEquals(negCollection, t.getValue());
    }
}
