package org.terasology.world.chunks.blockdata;

public final class TeraArrays {

    private TeraArrays() {}

    public static final byte getLo(int value) {
        return (byte)(value & 0x0F);
    }
    
    public static final byte getHi(int value) {
        return (byte)((value & 0xF0) >> 4);
    }
    
    public static final byte setHi(int value, int hi) {
        return makeByte(hi, getLo(value));
    }
    
    public static final byte setLo(int value, int lo) {
        return makeByte(getHi(value), lo);
    }
    
    public static final byte makeByte(int hi, int lo) {
        return (byte)((hi << 4) | (lo));
    }
}
