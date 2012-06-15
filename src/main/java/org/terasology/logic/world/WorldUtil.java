package org.terasology.logic.world;

import org.terasology.model.structures.BlockPosition;

import javax.vecmath.Vector3f;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class WorldUtil {

    private WorldUtil() {
    }

    /**
     * Gather the surrounding block positions
     * and order those by the distance to the originating point.
     */
    public static List<BlockPosition> gatherAdjacentBlockPositions(Vector3f origin) {

        ArrayList<BlockPosition> blockPositions = new ArrayList<BlockPosition>();

        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    int blockPosX = (int) (origin.x + (origin.x >= 0 ? 0.5f : -0.5f)) + x;
                    int blockPosY = (int) (origin.y + (origin.y >= 0 ? 0.5f : -0.5f)) + y;
                    int blockPosZ = (int) (origin.z + (origin.z >= 0 ? 0.5f : -0.5f)) + z;

                    blockPositions.add(new BlockPosition(blockPosX, blockPosY, blockPosZ, origin));
                }
            }
        }

        // Sort the block positions
        Collections.sort(blockPositions);
        return blockPositions;
    }

    public static void deleteWorld(File world) {
        if (world.isDirectory()) {
            String[] children = world.list();
            for (String element : children) {
                File f = new File(world, element);
                deleteWorld(f);
            }
            world.delete();
        } else world.delete();
    }
}
