package org.terasology.codecity.world.map;

import java.util.Set;

import org.terasology.codecity.world.structure.scale.CodeScale;

public interface CodeMap {

    /**
     * @return True if the map is empty
     */
    public boolean isEmpty();

    /**
     * @return The current size of the map in World coordinates
     */
    public int getSize();

    /**
     * Verify if in a given position a object exist
     * 
     * @param x
     *            Coordinate x to be used
     * @param z
     *            Coordinate y to be used
     * @return
     */
    public boolean isUsed(int x, int z);

    /**
     * Insert a object in the map.
     * 
     * @param content
     *            Object to be added
     * @param scale
     *            Scale to be used
     * @param x
     *            Coordinate x of the object
     * @param z
     *            Coordinate z of the object
     * @throws IllegalArgumentException
     *             if the object can't be added in position (x,z)
     */
    public void insertContent(DrawableCode content, CodeScale scale,
            CodeMapFactory factory, int x, int z);

    /**
     * Verify if the object can be placed in the given position
     * 
     * @param content
     *            Object to be placed
     * @param scale
     *            Scale to be used
     * @param x
     *            x Coordinate x of the object
     * @param z
     *            x Coordinate z of the object
     * @return True if can be placed, false otherwise
     */
    public boolean canPlaceContent(DrawableCode content, CodeScale scale,
            CodeMapFactory factory, int x, int z);

    /**
     * @return The set of unique MapObjects in the map
     */
    public Set<MapObject> getMapObjects();
    
    /**
     * @param x Coordinate x of the map
     * @param y Coordinate y of the map
     * @return The MaṕObject in the indicated position
     */
    public MapObject getMapObject(int x, int y);
    
}
