package org.terasology.world.chunks.blockdata;

import java.util.Arrays;

public class TeraSparseArray8Bit extends TeraSparseArrayByte {

    public TeraSparseArray8Bit() {
        super();
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, 8);
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, 8, inflated, deflated);
    }

    @Override
    public TeraArray pack() {
        byte[][] inf = new byte[sizeY][];
        byte[] def = new byte[sizeY];
        int alreadyPacked = 0, newPacked = 0;
        System.arraycopy(deflated, 0, def, 0, sizeY);
        for (int y = 0; y < sizeY; y++) {
            byte[] values = inflated[y];
            if (values != null) {
                byte first = values[0];
                boolean packable = true;
                for (int i = 1; i < sizeXZ; i++) {
                    if (values[i] != first) {
                        packable = false;
                        break;
                    }
                }
                if (packable) {
                    def[y] = first;
                    ++newPacked;
                } else {
                    inf[y] = new byte[sizeY];
                    System.arraycopy(values, 0, inf[y], 0, sizeXZ);
                    def[y] = 0;
                }
            } else {
                ++alreadyPacked;
            }
        }
        if (newPacked > 0) 
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inf, def);
        return this;
        // TODO switch back to dense array if necessary
    }

    @Override
    public TeraArray copy() {
        byte[][] inf = new byte[sizeY][];
        byte[] def = new byte[sizeY];
        for (int y = 0; y < sizeY; y++) {
            if (inflated[y] != null) {
                inf[y] = new byte[sizeXZ];
                System.arraycopy(inflated[y], 0, inf[y], 0, sizeXZ);
            }
        }
        System.arraycopy(deflated, 0, def, 0, sizeY);
        return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inf, def);
    }

    @Override
    public int get(int x, int y, int z) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
        byte[] data = inflated[y];
        if (data != null) 
            return data[z * sizeX + x];
        return deflated[y];
    }

    @Override
    public int set(int x, int y, int z, int value) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
        if (value < 0 || value > 255) throw new IllegalArgumentException();
        byte[] data = inflated[y];
        int pos = z * sizeX + x;
        if (data != null) {
            int old = data[pos] & 0xFF;
            data[pos] = (byte) value;
            return old;
        }
        byte old = deflated[y];
        if (old == (byte) value) {
            return value;
        }
        data = new byte[sizeXZ];
        Arrays.fill(data, old);
        data[pos] = (byte) value;
        inflated[y] = data;
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
        if (value < 0 || value > 255 || expected < 0 || expected > 255) throw new IllegalArgumentException();
        if (value == expected) return true;
        byte[] data = inflated[y];
        int pos = z * sizeX + x;
        if (data != null) {
            int old = data[pos] & 0xFF;
            if (old == expected)
                data[pos] = (byte) value;
            return old == expected;
        }
        byte old = deflated[y];
        if (old == (byte) expected) {
            data = new byte[sizeXZ];
            Arrays.fill(data, old);
            data[pos] = (byte) value;
            inflated[y] = data;
            return true;
        }
        return false;
    }

}
