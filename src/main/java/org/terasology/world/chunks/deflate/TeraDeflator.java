package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * TeraDeflator is the abstract base class used to implement chunk deflation.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraDeflator {

    public TeraDeflator() {}

    public abstract TeraArray deflate(final TeraArray in);
    
}
