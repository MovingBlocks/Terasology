package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;

import com.google.common.base.Preconditions;

public abstract class TeraAdvancedDeflator extends TeraDeflator {

    public TeraAdvancedDeflator() {}

    @Override
    public final TeraArray deflate(TeraArray in) {
        TeraArray result = Preconditions.checkNotNull(in).deflate(this);
        if (result != null)
            return result;
        return in;
    }

    public abstract TeraArray deflateDenseArray8Bit(final byte[] data, final int rowSize, final int sizeX, final int sizeY, final int sizeZ);
    
    public abstract TeraArray deflateDenseArray4Bit(final byte[] data, final int rowSize, final int sizeX, final int sizeY, final int sizeZ);
    
    public abstract TeraArray deflateSparseArray8Bit(final byte[][] inflated, final byte[] deflated, final byte fill, final int rowSize, final int sizeX, final int sizeY, final int sizeZ);
    
    public abstract TeraArray deflateSparseArray4Bit(final byte[][] inflated, final byte[] deflated, final byte fill, final int rowSize, final int sizeX, final int sizeY, final int sizeZ);
    
    public abstract TeraArray deflateDefault(final TeraArray in);
}