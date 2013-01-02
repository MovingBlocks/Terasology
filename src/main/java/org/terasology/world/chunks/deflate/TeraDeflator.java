package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;

public abstract class TeraDeflator {

    public TeraDeflator() {}

    public abstract TeraArray deflate(final TeraArray in);
    
}
