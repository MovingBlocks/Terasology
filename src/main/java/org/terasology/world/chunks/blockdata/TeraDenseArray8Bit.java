package org.terasology.world.chunks.blockdata;

public class TeraDenseArray8Bit extends TeraDenseArrayByte { 

    public TeraDenseArray8Bit() {
        this.data = new byte[0];
    }

    public TeraDenseArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, 8);
        this.data = new byte[sizeXYZ];
    }

    @Override
    public TeraArray deflate() {
        byte[][] inflated = new byte[sizeY][];
        byte[] deflated = new byte[sizeY];
        int packed = 0;
        for (int y = 0; y < sizeY; y++) {
            int start = y * sizeXZ;
            byte first = data[start];
            boolean packable = true;
            for (int i = 1; i < sizeXZ; i++) {
                if (data[start + i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                deflated[y] = first;
                ++packed;
            } else {
                byte[] tmp = new byte[sizeXZ];
                System.arraycopy(data, start, tmp, 0, sizeXZ);
                inflated[y] = tmp;
            }
        }
        if (packed == sizeY) {
            byte first = deflated[0];
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
        return this;
    }
    
    @Override
    public TeraArray inflate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TeraArray copy() {
        TeraDenseArrayByte result = new TeraDenseArray8Bit(sizeX, sizeY, sizeZ);
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }

    @Override
    public int get(int x, int y, int z) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        int pos = pos(x, y, z);
        return data[pos];
    }

    @Override
    public int set(int x, int y, int z, int value) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        int pos = pos(x, y, z);
        int old = data[pos];
        data[pos] = (byte) value;
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        if (expected < -128 || expected > 127) throw new IllegalArgumentException("Parameter 'expected' has to be in the range of -128 - 127 (" + value + ")");
        int pos = pos(x, y, z);
        int old = data[pos];
        if (old == expected) {
            data[pos] = (byte) value;
            return true;
        }
        return false;
    }

}
