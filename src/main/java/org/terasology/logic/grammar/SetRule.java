package org.terasology.logic.grammar;

import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tobias 'skaldarnar' Nett
 */
public class SetRule extends ComplexRule {
    // the AssetUri from the set command
    private BlockUri uri;

    public SetRule(BlockUri uri, float probability) {
        this(uri);
        this.probability = probability;
    }

    public SetRule(BlockUri uri) {
        this.uri = uri;
    }

    public List<Shape> getElements() {
        //Vector3i xAxis = coordinateSystem.getX();
        //Vector3i yAxis = coordinateSystem.getY();
        //Vector3i zAxis = coordinateSystem.getZ();
        BlockCollection collection = new BlockCollection();
        for (int x = position.x; x < position.x + dimension.x; x++) {
            for (int y = position.y; y < position.y + dimension.y; y++) {
                for (int z = position.z; z < position.z + dimension.z; z++) {
                    // transforming the relative coordinate system back
                    //  xAxis * x + yAxis * y + zAxis * z = pos
                    //int posX = xAxis.x * x + yAxis.x * y + zAxis.x * z;
                    //int posY = xAxis.y * x + yAxis.y * y + zAxis.y * z;
                    //int posZ = xAxis.z * x + yAxis.z * y + zAxis.z * z;

                    //BlockPosition blockPosition = new BlockPosition(posX, posY, posZ);
                    BlockPosition blockPosition = new BlockPosition(x, y, z);
                    collection.addBlock(blockPosition, BlockManager.getInstance().getBlock(uri));
                }
            }
        }
        Shape retVal = new TerminalShape(collection);
        retVal.setPosition(position);
        retVal.setCoordinateSystem(coordinateSystem);
        // retVal.setCoordinateSystem(CoordinateSystem.cartesianSystem());
        retVal.setDimension(dimension);
        return Arrays.asList(retVal);
    }

    public String toString() {
        return "Set ( \"" + uri.toString() + "\" );";
    }
}
