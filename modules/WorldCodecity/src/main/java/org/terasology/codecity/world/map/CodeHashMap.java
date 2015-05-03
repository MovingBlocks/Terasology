package org.terasology.codecity.world.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.terasology.codecity.world.structure.scale.CodeScale;
import org.terasology.math.Vector2i;

import com.google.common.base.Preconditions;

public class CodeHashMap implements CodeMap {
    private HashMap<String, MapObject> contentMap;
    private HashMap<DrawableCode, Vector2i> codePosition;
    private int size = 0;

    /**
     * Create a new map representation of a set of CodeContent
     */
    public CodeHashMap() {
        contentMap = new HashMap<String, MapObject>();
        codePosition = new HashMap<DrawableCode, Vector2i>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return contentMap.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUsed(int x, int z) {
        return contentMap.containsKey(x + "," + z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertContent(DrawableCode content, CodeScale scale,
            CodeMapFactory factory, int x0, int y0) {
        Preconditions.checkArgument(
                canPlaceContent(content, scale, factory, x0, y0),
                "Content must be placed in a valida position");

        int buildingSize = content.getSize(scale, factory);
        int xMax = x0 + buildingSize;
        int yMax = y0 + buildingSize;
        updateSize(xMax, yMax);

        codePosition.put(content, new Vector2i(x0, y0));
        for (int i = 0; i < buildingSize; i++) {
            for (int j = 0; j < buildingSize; j++) {
                int x = i + x0;
                int y = j + y0;
                contentMap.put(x + "," + y, new MapObject(content, x, y));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPlaceContent(DrawableCode content, CodeScale scale,
            CodeMapFactory factory, int x, int y) {
        int buildingSize = content.getSize(scale, factory);
        for (int i = x; i < buildingSize+x; i++)
            for (int j = y; j < buildingSize+y; j++)
                if (!canPlaceInPosition(x, y))
                    return false;
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @return
     */
    @Override
    public Set<MapObject> getMapObjects() {
        Set<MapObject> objects = new HashSet<MapObject>();
        objects.addAll(contentMap.values());
        return objects;
    }
    
    
    public MapObject getMapObject(int x, int y){
    	return contentMap.get(x + "," + y);
    }

    /**
     * Update the size of the map if needed
     * 
     * @param x
     *            Coordinate x to be used
     * @param y
     *            Coordinate y to be used
     */
    private void updateSize(int x, int y) {
        size = (x > size) ? x : size;
        size = (y > size) ? y : size;
    }

    /**
     * Verify if an object can be placed in the given position
     * 
     * @param x
     *            Coordinate x to be used
     * @param y
     *            Coordinate y to be used
     * @return
     */
    private boolean canPlaceInPosition(int x, int y) {
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                if (isUsed(x + i, y + j))
                    return false;
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Vector2i getCodePosition(DrawableCode code) {
        return codePosition.get(code);
    }
}
