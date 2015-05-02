package org.terasology.codecity.world.structure;

import org.terasology.codecity.world.map.DrawableCode;

/**
 * This class show the size of a portion of the code.
 */
public interface CodeRepresentation {

    /**
     * Generate a Drawable version of the content
     * 
     * @return
     */
    public DrawableCode getDrawableCode();
}
