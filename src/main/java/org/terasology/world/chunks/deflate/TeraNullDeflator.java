package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * TeraNullDeflator performs no deflation at all. It just returns the passed array.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class TeraNullDeflator extends TeraDeflator {

    public TeraNullDeflator() {}

    @Override
    public TeraArray deflate(TeraArray in) {
        return in;
    }

}
