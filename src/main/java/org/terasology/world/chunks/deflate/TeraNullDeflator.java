package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;

public class TeraNullDeflator extends TeraDeflator {

    public TeraNullDeflator() {}

    @Override
    public TeraArray deflate(TeraArray in) {
        return in;
    }

}
