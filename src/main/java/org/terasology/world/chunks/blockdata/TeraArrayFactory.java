package org.terasology.world.chunks.blockdata;

/**
 * This is the interface for tera array factories. Every tera array is required to implement a factory.
 * It should be implemented as a static subclass of the corresponding tera array class and it should be called Factory.
 *  
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.Factory
 *
 */
public interface TeraArrayFactory<T extends TeraArray> {

    public Class<T> getArrayClass();

    public T create();
    
    public T create(int sizeX, int sizeY, int sizeZ);
    
}
