package org.terasology.world.chunks.deflate;

import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray8Bit;

public class TeraStandardDeflator extends TeraAdvancedDeflator {
    
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
      if (packed >= 4) {
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
        if (packed >= 4) {
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

    @Override
    public TeraArray deflateDefault(TeraArray in) {
        return null;
    }
}
