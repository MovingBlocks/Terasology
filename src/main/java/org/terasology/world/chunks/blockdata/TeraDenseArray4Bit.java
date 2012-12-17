package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;

import com.google.common.base.Preconditions;


public class TeraDenseArray4Bit extends TeraDenseArrayByte {

    protected int sizeXYZHalf;

    @Override
    protected void readExternalHeader(ObjectInput in) throws IOException {
        super.readExternalHeader(in);
        sizeXYZHalf = sizeXYZ / 2;
    }

    public TeraDenseArray4Bit() {
        data = new byte[0];
    }

    public TeraDenseArray4Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, 4);
        Preconditions.checkArgument(sizeXYZ % 2 == 0, String.format("The total size has to be a multiple of 2 (%d)", sizeXYZ));
        sizeXYZHalf = sizeXYZ / 2;
        data = new byte[sizeXYZHalf];
    }

    @Override
    public TeraArray deflate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TeraArray inflate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TeraArray copy() {
        TeraDenseArrayByte result = new TeraDenseArray4Bit(sizeX, sizeY, sizeZ);
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        return data.length;
    }

    @Override
    public int get(int x, int y, int z) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        int pos = pos(x, y, z);
        if (pos < sizeXYZHalf) {
            int raw = data[pos] & 0xFF;
            return (byte) ((raw & 0x0F) & 0xFF);
        }
        int raw = data[pos - sizeXYZHalf] & 0xFF;
        return (byte) (raw >> 4);
    }

    @Override
    public int set(int x, int y, int z, int value) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < 0 || value > 15) throw new IllegalArgumentException("Parameter 'value' has to be in the range 0 - 15 (" + value + ")");
        int pos = pos(x, y, z);
        if (pos < sizeXYZHalf) {
            int raw = data[pos] & 0xFF;
            int tmp = value & 0xFF;
            byte old = (byte) ((raw & 0x0F) & 0xFF);
            data[pos] = (byte) ((tmp & 0x0F) | (raw & 0xF0));
            return old;
        }
        pos = pos - sizeXYZHalf;
        int raw = data[pos] & 0xFF;
        int tmp = value & 0xFF;
        byte old = (byte) (raw >> 4);
        data[pos] = (byte) ((raw & 0x0F) | (tmp << 4) & 0xFF);
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < 0 || value > 15) throw new IllegalArgumentException("Parameter 'value' has to be in the range 0 - 15 (" + value + ")");
        if (expected < 0 || expected > 15) throw new IllegalArgumentException("Parameter 'expected' has to be in the range 0 - 15 (" + value + ")");
        int pos = pos(x, y, z);
        if (pos < sizeXYZHalf) {
            int raw = data[pos] & 0xFF;
            byte old = (byte) ((raw & 0x0F) & 0xFF);
            if (old == expected) {
                int tmp = value & 0xFF;
                data[pos] = (byte) ((tmp & 0x0F) | (raw & 0xF0));
                return true;
            }
            return false;
        }
        int raw = data[pos % sizeXYZHalf] & 0xFF;
        byte old = (byte) (raw >> 4);
        if (old == expected) {
            int tmp = value & 0xFF;
            data[pos % sizeXYZHalf] = (byte) ((raw & 0x0F) | (tmp << 4) & 0xFF);
            return true;
        }
        return false;
    }

}
