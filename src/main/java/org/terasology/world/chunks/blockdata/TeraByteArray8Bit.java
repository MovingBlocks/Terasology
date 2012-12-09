package org.terasology.world.chunks.blockdata;

public class TeraByteArray8Bit extends TeraByteArray { 
  
  public TeraByteArray8Bit() {
    this.data = new byte[0];
  }
  
  public TeraByteArray8Bit(int sizeX, int sizeY, int sizeZ) {
    super(sizeX, sizeY, sizeZ, 8);
    this.data = new byte[sizeY * sizeX * sizeZ];
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

//  if (!contains(x, y, z)) 
//  throw new IndexOutOfBoundsException(String.format("size=(%d, %d, %d), pos=(%d, %d, %d)", sizeX, sizeY, sizeZ, x, y, z));
    
    int pos = pos(x, y, z);
    return data[pos] & 0xFF;
  }

  @Override
  public int set(int x, int y, int z, int value) {
    if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
    if (value < 0 || value > 255) throw new IllegalArgumentException();

//    if (!contains(x, y, z))  
//      throw new IndexOutOfBoundsException(String.format("size=(%d, %d, %d), pos=(%d, %d, %d)", sizeX, sizeY, sizeZ, x, y, z));
//    Preconditions.checkArgument(value >= 0 && value < 256, String.format("Parameter 'value' has to be in the range 0 - 255 (%d)", value));

    int pos = pos(x, y, z);
    int old = data[pos] & 0xFF;
    data[pos] = (byte) value;
    return old;
  }

  @Override
  public boolean set(int x, int y, int z, int value, int expected) {
    if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
    if (value < 0 || value > 255 || expected < 0 || expected > 255) throw new IllegalArgumentException();

//    if (!contains(x, y, z))  
//      throw new IndexOutOfBoundsException(String.format("size=(%d, %d, %d), pos=(%d, %d, %d)", sizeX, sizeY, sizeZ, x, y, z));
//    Preconditions.checkArgument(value >= 0 && value < 256, String.format("Parameter 'value' has to be in the range 0 - 255 (%d)", value));
//    Preconditions.checkArgument(value >= 0 && value < 256, String.format("Parameter 'expected' has to be in the range 0 - 255 (%d)", expected));

    int pos = pos(x, y, z);
    int old = data[pos] & 0xFF;
    if (old == expected) {
      data[pos] = (byte) value;
      return true;
    }
    return false;
  }

}
