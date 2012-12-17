package org.terasology.world.chunks.blockdata;

public class TeraByteArray8Bit extends TeraByteArray { 

    public TeraByteArray8Bit() {
        this.data = new byte[0];
    }

    public TeraByteArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, 8);
        this.data = new byte[sizeXYZ];
    }

    @Override
    public TeraArray pack() {
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
        if (packed >= 5) {
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }
        return this;
    }

    @Override
    public TeraArray copy() {
        TeraByteArray result = new TeraByteArray8Bit(sizeX, sizeY, sizeZ);
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }

    @Override
    public int get(int x, int y, int z) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
        int pos = pos(x, y, z);
        return data[pos] & 0xFF;
    }

    @Override
    public int set(int x, int y, int z, int value) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
        if (value < 0 || value > 255) throw new IllegalArgumentException();
        int pos = pos(x, y, z);
        int old = data[pos] & 0xFF;
        data[pos] = (byte) value;
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
        if (value < 0 || value > 255 || expected < 0 || expected > 255) throw new IllegalArgumentException();
        int pos = pos(x, y, z);
        int old = data[pos] & 0xFF;
        if (old == expected) {
            data[pos] = (byte) value;
            return true;
        }
        return false;
    }

}
