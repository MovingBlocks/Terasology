package org.terasology.world.block;

import com.google.common.collect.Maps;
import org.terasology.world.block.shapes.BlockMeshPart;

import javax.vecmath.Vector2f;
import java.util.EnumMap;
import java.util.Map;

/**
 * A block's appearance.
 *
 * @author Immortius
 */
public class BlockAppearance {

    private Map<BlockPart, BlockMeshPart> blockParts;
    private Map<BlockPart, Vector2f> textureAtlasPos = new EnumMap<>(BlockPart.class);

    public BlockAppearance() {
        blockParts = Maps.newEnumMap(BlockPart.class);
        textureAtlasPos = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            textureAtlasPos.put(part, new Vector2f());
        }
    }

    public BlockAppearance(Map<BlockPart, BlockMeshPart> blockParts, Map<BlockPart, Vector2f> textureAtlasPos) {
        this.blockParts = blockParts;
        this.textureAtlasPos = textureAtlasPos;
        for (BlockPart part : BlockPart.values()) {
            if (textureAtlasPos == null) {
                textureAtlasPos.put(part, new Vector2f());
            }
        }
    }

    public BlockMeshPart getPart(BlockPart part) {
        return blockParts.get(part);
    }

    public Vector2f getTextureAtlasPos(BlockPart part) {
        return new Vector2f(textureAtlasPos.get(part));
    }

}
