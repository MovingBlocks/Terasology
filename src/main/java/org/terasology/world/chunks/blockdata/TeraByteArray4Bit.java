package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.common.base.Preconditions;


public class TeraByteArray4Bit extends TeraByteArray {

  protected int sizeXYZHalf;
  
  @Override
  protected void writeExternalHeader(ObjectOutput out) throws IOException {
    super.writeExternalHeader(out);
    out.writeInt(sizeXYZHalf);
  }
  
  @Override
  protected void readExternalHeader(ObjectInput in) throws IOException {
    super.readExternalHeader(in);
    sizeXYZHalf = in.readInt();
  }

  public TeraByteArray4Bit() {
    data = new byte[0];
  }

  public TeraByteArray4Bit(int sizeX, int sizeY, int sizeZ) {
    super(sizeX, sizeY, sizeZ, 4);
    Preconditions.checkArgument(sizeXYZ % 2 == 0, String.format("The total size has to be a multiple of 2 (%d)", sizeXYZ));
    sizeXYZHalf = sizeXYZ / 2;
    data = new byte[sizeXYZHalf];
  }

  @Override
  public TeraArray copy() {
    TeraByteArray result = new TeraByteArray4Bit(sizeX, sizeY, sizeZ);
    System.arraycopy(data, 0, result.data, 0, data.length);
    return result;
  }
  
  @Override
  public int estimatedMemoryConsumptionInBytes() {
    return data.length;
  }

  @Override
  public int get(int x, int y, int z) {
    if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
    
//    if (!contains(x, y, z)) 
//      throw new IndexOutOfBoundsException(String.format("size=(%d, %d, %d), pos=(%d, %d, %d)", sizeX, sizeY, sizeZ, x, y, z));
    
    int pos = pos(x, y, z);
    
    if (pos < sizeXYZHalf) {
      int raw = data[pos] & 0xFF;
      return (byte) ((raw & 0x0F) & 0xFF);
    }

    int raw = data[pos % sizeXYZHalf] & 0xFF;
    return (byte) (raw >> 4);
  }

  @Override
  public int set(int x, int y, int z, int value) {
    if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
    if (value < 0 || value > 15) throw new IllegalArgumentException();
    
//  if (!contains(x, y, z)) 
//  throw new IndexOutOfBoundsException(String.format("size=(%d, %d, %d), pos=(%d, %d, %d)", sizeX, sizeY, sizeZ, x, y, z));
//Preconditions.checkArgument(value >= 0 && value < 16, String.format("Parameter 'value' has to be in the range 0 - 15 (%d)", value));
    
    int pos = pos(x, y, z);
    
    if (pos < sizeXYZHalf) {
      int raw = data[pos] & 0xFF;
      int tmp = value & 0xFF;
      byte old = (byte) ((raw & 0x0F) & 0xFF);
      data[pos] = (byte) ((tmp & 0x0F) | (raw & 0xF0));
      return old;
    }

    int raw = data[pos % sizeXYZHalf] & 0xFF;
    int tmp = value & 0xFF;
    byte old = (byte) (raw >> 4);
    data[pos % sizeXYZHalf] = (byte) ((raw & 0x0F) | (tmp << 4) & 0xFF);
    return old;
  }

  @Override
  public boolean set(int x, int y, int z, int value, int expected) {
    if (!contains(x, y, z)) throw new IndexOutOfBoundsException();
    if (value < 0 || value > 15 || expected < 0 || expected > 15) throw new IllegalArgumentException();
    
//    if (!contains(x, y, z)) 
//      throw new IndexOutOfBoundsException(String.format("size=(%d, %d, %d), pos=(%d, %d, %d)", sizeX, sizeY, sizeZ, x, y, z));
//    Preconditions.checkArgument(value >= 0 && value < 16, String.format("Parameter 'value' has to be in the range 0 - 15 (%d)", value));
//    Preconditions.checkArgument(expected >= 0 && expected < 16, String.format("Parameter 'expected' has to be in the range 0 - 15 (%d)", expected));
    
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
