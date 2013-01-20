package org.terasology.grammarSystem.logic.grammar.shapes.complex;

import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.TerminalShape;
import org.terasology.math.Matrix4i;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tobias 'skaldarnar' Nett
 *         <p/>
 *         A SetRule sets all blocks in the current scope to the specified block type. The block type is specified by the {@link BlockUri}.
 */
public class SetRule extends ComplexRule {
    /** the AssetUri from the set command */
    private BlockUri uri;

    /**
     * Init. a SetRule with the given block type and probability.
     * <p/>
     * A SetRule is of the for A ::- Set("engine:stone") : _probability_; and describes the substitution of all blocks in the shapes bounds
     * with the specified type.
     *
     * @param uri         the block type
     * @param probability the rule probability
     */
    public SetRule(BlockUri uri, float probability) {
        this(uri);
        this.probability = probability;
    }

    /**
     * Init. a SetRule with the given block type and probability 1.
     * <p/>
     * A SetRule is of the for A ::- Set("engine:stone") : _probability_; and describes the substitution of all blocks in the shapes bounds
     * with the specified type.
     *
     * @param uri the block type
     */
    public SetRule(BlockUri uri) {
        this.uri = uri;
        matrix.identity();
    }

    /**
     * Constructs a new TerminalShape which is filled by blocks of the type specified by this SetRule. The resulting list contains exactly
     * one element.
     *
     * @return the resulting TerminalShape
     */
    public List<Shape> getElements() {
        BlockCollection collection = new BlockCollection();
        for (int x = 0; x < Math.abs(dimension.x); x++) {
            for (int y = 0; y < Math.abs(dimension.y); y++) {
                for (int z = 0; z < Math.abs(dimension.z); z++) {
                    // transforming the relative coordinate system back
                    int _x = (dimension.x < 0) ? -x : x;
                    int _y = (dimension.y < 0) ? -y : y;
                    int _z = (dimension.z < 0) ? z : -z;
                    Vector3i pos = new Vector3i(_x, _y, _z);
                    matrix.transformPoint(pos);
                    BlockPosition blockPosition = new BlockPosition(pos.x, pos.y, pos.z);
                    collection.addBlock(blockPosition, BlockManager.getInstance().getBlock(uri));
                }
            }
        }
        //collection.setAttachPos(new BlockPosition(matrix.getTranslation().x, matrix.getTranslation().y, matrix.getTranslation().z));
        Shape retVal = new TerminalShape(collection);
        retVal.setDimension(this.dimension);
        return Arrays.asList(retVal);
    }

    public String toString() {
        return "Set ( \"" + uri.toString() + "\" );";
    }

    @Override
    public Shape clone() {
        SetRule clone = new SetRule(uri, probability);
        clone.setActive(active);
        clone.setMatrix(new Matrix4i(matrix));
        clone.setDimension(new Vector3i(dimension));
        return clone;
    }
}
