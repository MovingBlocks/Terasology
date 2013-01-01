package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray8Bit;

public class TeraStandardDeflator extends TeraAdvancedDeflator {
    
    /*
     *  8-bit variant
     *  =============
     *  
     *  dense chunk  : 4 + 12 + 65536                                                   = 65552
     *  sparse chunk : (4 + 12 + 256) + (4 + 12 + (256 × 4)) + ((12 + 256) × 256)       = 69920
     *  difference   : 69920 - 65552                                                    =  4368
     *  min. deflate : 4368 / (12 + 256)                                                =    16.3
     *  
     *  
     *  4-bit variant
     *  =============
     *  
     *  dense chunk  : 4 + 12 + (65536 / 2)                                             = 32784
     *  sparse chunk : (4 + 12 + 256) + (4 + 12 + (256 × 4)) + ((12 + (256 / 2)) × 256) = 37152
     *  difference   : 37152 - 32784                                                    =  4368
     *  min. deflate : 4368 / (12 + (256 / 2))                                          =    31.2
     *  
     */

    // TODO dynamically calculate DEFLATE_MINIMUM_8BIT and DEFLATE_MINIMUM_4BIT, they only work for chunks with dimension 16x256x16
    protected final static int DEFLATE_MINIMUM_8BIT = 16;
    protected final static int DEFLATE_MINIMUM_4BIT = 31;
    
    public TeraStandardDeflator() {}

    @Override
    public TeraArray deflateDenseArray8Bit(final byte[] data, final int rowSize, final int sizeX, final int sizeY, final int sizeZ) {
      final byte[][] inflated = new byte[sizeY][];
      final byte[] deflated = new byte[sizeY];
      int packed = 0;
      for (int y = 0; y < sizeY; y++) {
          final int start = y * rowSize;
          final byte first = data[start];
          boolean packable = true;
          for (int i = 1; i < rowSize; i++) {
              if (data[start + i] != first) {
                  packable = false;
                  break;
              }
          }
          if (packable) {
              deflated[y] = first;
              ++packed;
          } else {
              byte[] tmp = new byte[rowSize];
              System.arraycopy(data, start, tmp, 0, rowSize);
              inflated[y] = tmp;
          }
      }
      if (packed == sizeY) {
          final byte first = deflated[0];
          boolean packable = true;
          for (int i = 1; i < sizeY; i++) {
              if (deflated[i] != first) {
                  packable = false;
                  break;
              }
          }
          if (packable)
              return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, first);
      }
      if (packed > DEFLATE_MINIMUM_8BIT) {
          return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inflated, deflated);
      }
      return null;
    }

    @Override
    public TeraArray deflateDenseArray4Bit(final byte[] data, final int rowSize, final int sizeX, final int sizeY, final int sizeZ) {
        final byte[][] inflated = new byte[sizeY][];
        final byte[] deflated = new byte[sizeY];
        int packed = 0;
        for (int y = 0; y < sizeY; y++) {
            final int start = y * rowSize;
            final byte first = data[start];
            boolean packable = true;
            for (int i = 1; i < rowSize; i++) {
                if (data[start + i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                deflated[y] = first;
                ++packed;
            } else {
                byte[] tmp = new byte[rowSize];
                System.arraycopy(data, start, tmp, 0, rowSize);
                inflated[y] = tmp;
            }
        }
        if (packed == sizeY) {
            final byte first = deflated[0];
            boolean packable = true;
            for (int i = 1; i < sizeY; i++) {
                if (deflated[i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable)
                return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ, first);
        }
        if (packed > DEFLATE_MINIMUM_4BIT) {
            return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }
        return null;
    }

    @Override
    public TeraArray deflateSparseArray8Bit(final byte[][] inflated, final byte[] deflated, final byte fill, final int rowSize, final int sizeX, final int sizeY, final int sizeZ) {
        return null;
    }

    @Override
    public TeraArray deflateSparseArray4Bit(final byte[][] inflated, final byte[] deflated, final byte fill, final int rowSize, final int sizeX, final int sizeY, final int sizeZ) {
        return null;
    }

}
