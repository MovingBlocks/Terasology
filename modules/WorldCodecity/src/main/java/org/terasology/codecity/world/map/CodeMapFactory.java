package org.terasology.codecity.world.map;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.terasology.codecity.world.structure.scale.CodeScale;
import org.terasology.codecity.world.structure.scale.SquaredCodeScale;

/**
 * This class is in charge of creating maps in base of a list of code.
 */
public class CodeMapFactory {
    private CodeScale scale;

    /**
     * Create a new CodeMapFactory using a default scale
     */
    public CodeMapFactory() {
        scale = new SquaredCodeScale();
    }

    /**
     * Create a new CodeMapFactory
     * 
     * @param scale
     *            Scale to be used in the generation of the map
     */
    public CodeMapFactory(CodeScale scale) {
        this.scale = scale;
    }

    /**
     * Generate a new map with the given list of content
     * 
     * @param contentList
     *            Content of the map
     * @return New map in base of the given content
     */
    public CodeMap generateMap(List<DrawableCode> contentList) {
        // Sort the content by scale
        Collections.sort(contentList, new DrawableCodeSizeComparator(scale,
                this));

        // Start drawing in the map
        CodeMap map = new CodeHashMap();

        for (DrawableCode content : contentList)
            insertInMap(map, content);

        return map;
    }

    /**
     * Insert an object in the map
     * 
     * @param map
     *            Map where the object will be inserted
     * @param content
     *            Object to be added
     */
    private void insertInMap(CodeMap map, DrawableCode content) {
        if (map.isEmpty()) {
            map.insertContent(content, scale, this, 0, 0);
            return;
        }

        int z = 0;
        while (true) {
            for (int x = 0; x < map.getSize(); x++) {
                if (map.canPlaceContent(content, scale, this, x, z)) {
                    map.insertContent(content, scale, this, x, z);
                    return;
                }
            }
            z += 1;
        }

    }
}

/**
 * This class is used to compare two DrawableCode objects
 */
class DrawableCodeSizeComparator implements Comparator<DrawableCode> {
    private CodeScale scale;
    private CodeMapFactory factory;

    public DrawableCodeSizeComparator(CodeScale scale, CodeMapFactory factory) {
        this.scale = scale;
        this.factory = factory;
    }

    @Override
    public int compare(DrawableCode c1, DrawableCode c2) {
        return c1.getSize(scale, factory) - c2.getSize(scale, factory);
    }

}