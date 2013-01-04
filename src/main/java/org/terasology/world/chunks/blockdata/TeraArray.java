package org.terasology.world.chunks.blockdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.terasology.world.chunks.deflate.TeraVisitingDeflator;

import com.google.common.base.Preconditions;


/**
 * TeraArray is the base class used to store block related data in Chunks.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraArray implements Externalizable {

    private int sizeX, sizeY, sizeZ, sizeXZ, sizeXZHalf, sizeXYZ, sizeXYZHalf;

    protected final void writeExternalHeader(ObjectOutput out) throws IOException {
        out.writeInt(sizeX);
        out.writeInt(sizeY);
        out.writeInt(sizeZ); 
    }

    protected final void readExternalHeader(ObjectInput in) throws IOException {
        sizeX = in.readInt();
        sizeY = in.readInt();
        sizeZ = in.readInt();
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
    }
    
    protected final int pos(int x, int y, int z) {
        return y * getSizeXZ() + z * getSizeX() + x;
    }

    protected final int pos(int x, int z) {
        return z * getSizeX() + x;
    }
    
    protected abstract void initialize();
    
    
    /**
     * Extending this class is the recommended way to implement serialization handlers for tera arrays.
     * Tera arrays should implement their serialization handlers as a static subclass called SerializationHandler.
     * 
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.SerializationHandler
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.Factory
     *
     */
    protected static abstract class SerializationHandler<T extends TeraArray> implements TeraArraySerializationHandler<T> {

        protected abstract void internalSerialize(T array, DataOutputStream out) throws IOException;
        
        protected abstract void internalDeserialize(T array, DataInputStream in) throws IOException;

        @Override
        public final void serialize(T array, OutputStream out) throws IOException {
            Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
            Preconditions.checkNotNull(out, "The parameter 'out' must not be null");
            final DataOutputStream dout = new DataOutputStream(out);
            try {
                dout.writeInt(array.sizeX);
                dout.writeInt(array.sizeY);
                dout.writeInt(array.sizeZ);
                internalSerialize(array, dout);
                dout.flush();
            } finally {
                dout.close();
            }
        }

        @Override
        public final T deserialize(TeraArrayFactory<T> factory, InputStream in) throws IOException {
            Preconditions.checkNotNull(factory, "The parameter 'factory' must not be null");
            Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
            final DataInputStream din = new DataInputStream(in);
            try {
                int sizeX = din.readInt();
                int sizeY = din.readInt();
                int sizeZ = din.readInt();
                T array = Preconditions.checkNotNull(factory.create(sizeX, sizeY, sizeZ), "TeraArrayFactory<T>:create(int, int, int) must not return null");
                internalDeserialize(array, din);
                return array;
            } finally {
                din.close();
            }
        }
        
    }

    protected TeraArray() {}

    protected TeraArray(int sizeX, int sizeY, int sizeZ, boolean initialize) {
        Preconditions.checkArgument(sizeX > 0);
        Preconditions.checkArgument(sizeY > 0);
        Preconditions.checkArgument(sizeZ > 0);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
        Preconditions.checkArgument(getSizeXYZ() % 2 == 0, "The product of the parameters 'sizeX', 'sizeY' and 'sizeZ' has to be a multiple of 2 (" + getSizeXYZ() + ")");
        Preconditions.checkArgument(getSizeXZ() % 2 == 0, "The product of the parameters 'sizeX' and 'sizeZ' has to be a multiple of 2 (" + getSizeXZ() + ")");
        if (initialize) initialize();
    }

    public final int getSizeX() {
        return sizeX;
    }

    public final int getSizeY() {
        return sizeY;
    }

    public final int getSizeZ() {
        return sizeZ;
    }

    public final int getSizeXZ() {
        return sizeXZ;
    }
    
    public final int getSizeXZHalf() {
        return sizeXZHalf;
    }

    public final int getSizeXYZ() {
        return sizeXYZ;
    }
    
    public final int getSizeXYZHalf() {
        return sizeXYZHalf;
    }

    public final boolean contains(int x, int y, int z) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ);
    }

    public abstract boolean isSparse();

    public abstract TeraArray copy();
    
    public abstract TeraArray deflate(TeraVisitingDeflator deflator);

    public abstract int getEstimatedMemoryConsumptionInBytes();
    
    public abstract int getElementSizeInBits();

    public abstract int get(int x, int y, int z);

    public abstract int set(int x, int y, int z, int value);

    public abstract boolean set(int x, int y, int z, int value, int expected);
    
}
