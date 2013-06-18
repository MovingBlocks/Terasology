package org.terasology.world.chunks.internal;

import gnu.trove.list.TIntList;
import gnu.trove.map.TByteObjectMap;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class ReadyChunkInfo {
    private Vector3i pos;
    private TByteObjectMap<TIntList> blockPositionMapppings;

    public ReadyChunkInfo(Vector3i pos, TByteObjectMap<TIntList> blockPositionMapppings) {
        this.pos = pos;
        this.blockPositionMapppings = blockPositionMapppings;
    }

    public Vector3i getPos() {
        return pos;
    }

    public TByteObjectMap<TIntList> getBlockPositionMapppings() {
        return blockPositionMapppings;
    }
}
