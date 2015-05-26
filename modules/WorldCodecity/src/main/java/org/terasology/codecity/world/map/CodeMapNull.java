package org.terasology.codecity.world.map;

import java.util.HashSet;
import java.util.Set;

import org.terasology.codecity.world.structure.scale.CodeScale;
import org.terasology.math.Vector2i;

/**
 * This class represent a null map that don't accept anything
 */
public class CodeMapNull implements CodeMap {

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public boolean isUsed(int x, int z) {
        return false;
    }

    @Override
    public void insertContent(DrawableCode content, CodeScale scale,
            CodeMapFactory factory, int x, int z) {

    }

    @Override
    public boolean canPlaceContent(DrawableCode content, CodeScale scale,
            CodeMapFactory factory, int x, int z) {
        return false;
    }

    @Override
    public Set<MapObject> getMapObjects() {
        return new HashSet<MapObject>();
    }

    @Override
    public MapObject getMapObject(int x, int y) {
        return null;
    }

    @Override
    public Vector2i getCodePosition(DrawableCode code) {
        // TODO Auto-generated method stub
        return null;
    }
}
