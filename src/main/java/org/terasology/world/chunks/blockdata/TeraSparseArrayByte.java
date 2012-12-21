package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.common.base.Preconditions;


/**
 * TeraSparseArrayByte is the base class used to implement sparse arrays with elements of size 4 bit or 8 bit.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraSparseArrayByte extends TeraSparseArray {

    protected byte[][] inflated;
    protected byte[] deflated;
    protected byte fill;
    
    protected abstract TeraArray createSparse(byte fill);
    
    protected abstract TeraArray createSparse(byte[][] inflated, byte[] deflated);

    protected abstract int rowSize();
    
    protected final int pos(int x, int z) {
        return z * getSizeX() + x;
    }

    public TeraSparseArrayByte() {
        super();
    }

    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ);
        this.inflated = Preconditions.checkNotNull(inflated);
        this.deflated = Preconditions.checkNotNull(deflated);
        Preconditions.checkArgument(inflated.length == sizeY);
        Preconditions.checkArgument(deflated.length == sizeY);
    }
    
    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ);
        this.fill = fill;
    }

    @Override
    public final int getEstimatedMemoryConsumptionInBytes() {
        if (inflated == null)
            return 9;
        int result = 9 + getSizeY() + (getSizeY() * 4);
        for (int i = 0; i < getSizeY(); i++) {
            if (inflated[i] != null)
                result += 12 + rowSize();
        }
        return result;
    }

    @Override
    public final TeraArray copy() {
        if (inflated == null) 
            return createSparse(fill);
        byte[][] inf = new byte[getSizeY()][];
        byte[] def = new byte[getSizeY()];
        for (int y = 0; y < getSizeY(); y++) {
            if (inflated[y] != null) {
                inf[y] = new byte[rowSize()];
                System.arraycopy(inflated[y], 0, inf[y], 0, rowSize());
            }
        }
        System.arraycopy(deflated, 0, def, 0, getSizeY());
        return createSparse(inf, def);
    }

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeObject(inflated);
        out.writeObject(deflated);
        out.writeByte(fill);
    }

    @Override
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        inflated = (byte[][]) in.readObject();
        deflated = (byte[]) in.readObject();
        fill = in.readByte();
    }
}
