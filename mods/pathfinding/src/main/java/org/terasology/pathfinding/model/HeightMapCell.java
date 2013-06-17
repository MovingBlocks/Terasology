package org.terasology.pathfinding.model;

import java.util.ArrayList;
import java.util.List;

/**
* @author synopia
*/
public class HeightMapCell {
    public final List<WalkableBlock> blocks = new ArrayList<WalkableBlock>();

    public void addBlock(WalkableBlock walkableBlock) {
        if( blocks.size()==0 ) {
            blocks.add(walkableBlock);
        } else {
            blocks.add(0, walkableBlock);
        }
    }

    public WalkableBlock getBlock(int height) {
        for (WalkableBlock block : blocks) {
            if( block.height()==height ) {
                return block;
            }
        }
        return null;
    }
}
