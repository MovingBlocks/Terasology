package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;

import com.google.common.base.Preconditions;

public abstract class TeraVisitingDeflator extends TeraDeflator {

    public TeraVisitingDeflator() {}

    @Override
    public final TeraArray deflate(TeraArray in) {
        TeraArray result = Preconditions.checkNotNull(in).deflate(this);
        if (result != null)
            return result;
        return in;
    }

    public abstract TeraArray deflateDenseArray16Bit(short[] data, int rowSize, int sizeX, int sizeY, int sizeZ);
    
    public abstract TeraArray deflateDenseArray8Bit(byte[] data, int rowSize, int sizeX, int sizeY, int sizeZ);
    
    public abstract TeraArray deflateDenseArray4Bit(byte[] data, int rowSize, int sizeX, int sizeY, int sizeZ);
    
    
    public abstract TeraArray deflateSparseArray16Bit(short[][] inflated, short[] deflated, short fill, int rowSize, int sizeX, int sizeY, int sizeZ);
    
    public abstract TeraArray deflateSparseArray8Bit(byte[][] inflated, byte[] deflated, byte fill, int rowSize, int sizeX, int sizeY, int sizeZ);
    
    public abstract TeraArray deflateSparseArray4Bit(byte[][] inflated, byte[] deflated, byte fill, int rowSize, int sizeX, int sizeY, int sizeZ);

}