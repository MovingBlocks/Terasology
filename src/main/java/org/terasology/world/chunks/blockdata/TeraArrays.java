package org.terasology.world.chunks.blockdata;

import java.nio.ByteBuffer;
import java.util.Map;

import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;


/**
 * TeraArrays is a utility class. Its methods are used in the implementation of specialized TeraArray's.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public final class TeraArrays {

    private static final Map<Class<? extends TeraArray>, TeraArray.SerializationHandler<? extends TeraArray>> arrayHandlers;
    private static final Map<Class<? extends TeraArray>, TeraArray.Factory<? extends TeraArray>> arrayFactories;
    private static final Map<Class<? extends TeraArray>, ChunksProtobuf.Type> arrayClassToType;
    private static final Map<ChunksProtobuf.Type, Class<? extends TeraArray>> arrayTypeToClass;
    private static final Map<String, Class<? extends TeraArray>> arrayNameToClass;
    
    static {
        arrayHandlers = Maps.newHashMap();
        arrayFactories = Maps.newHashMap();
        arrayClassToType = Maps.newHashMap();
        arrayTypeToClass = Maps.newHashMap();
        arrayNameToClass = Maps.newHashMap();
        register(new TeraDenseArray4Bit.Factory(), new TeraDenseArray4Bit.SerializationHandler(), Type.DenseArray4Bit);
        register(new TeraDenseArray8Bit.Factory(), new TeraDenseArray8Bit.SerializationHandler(), Type.DenseArray8Bit);
        register(new TeraDenseArray16Bit.Factory(), new TeraDenseArray16Bit.SerializationHandler(), Type.DenseArray16Bit);
        register(new TeraSparseArray4Bit.Factory(), new TeraSparseArray4Bit.SerializationHandler(), Type.SparseArray4Bit);
        register(new TeraSparseArray8Bit.Factory(), new TeraSparseArray8Bit.SerializationHandler(), Type.SparseArray8Bit);
        register(new TeraSparseArray16Bit.Factory(), new TeraSparseArray16Bit.SerializationHandler(), Type.SparseArray16Bit);
    }
    
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
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final ChunksProtobuf.TeraArray encode(TeraArray array) {
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        final Class<? extends TeraArray> cls = array.getClass();
        final ChunksProtobuf.TeraArray.Builder b = ChunksProtobuf.TeraArray.newBuilder();
        final ChunksProtobuf.Type t = Preconditions.checkNotNull(getProtobufType(cls), "The method TeraArray:getProtobufType() must not return null");
        final TeraArray.SerializationHandler handler = Preconditions.checkNotNull(getSerializationHandler(cls), "No serialization handler found for tera array of class: " + cls.getName());
        final ByteBuffer buf = handler.serialize(array, null);
        buf.rewind();
        b.setData(ByteString.copyFrom(buf));
        b.setType(t);
        if (t == ChunksProtobuf.Type.Unknown)
            b.setClassName(cls.getName());
        return b.build();
    }
    
    @SuppressWarnings("rawtypes")
    public static final TeraArray decode(ChunksProtobuf.TeraArray message) {
        Preconditions.checkNotNull(message);
        final ChunksProtobuf.Type t = message.getType();
        final ByteString data = Preconditions.checkNotNull(message.getData(), "Illformed protobuf message. ChunksProtobuf.TeraArray:getData() must not return null");
        final Class<? extends TeraArray> cls;
        if (t == ChunksProtobuf.Type.Unknown) {
            final String name = message.getClassName();
            Preconditions.checkNotNull(name, "Illformed protobuf message. ChunksProtobuf.TeraArray:getClassName() must not return null");
            cls = arrayNameToClass.get(name);
            Preconditions.checkNotNull(cls, "Unable to decode protobuf message. No array class found for name: " + name);
        } else { 
            cls = arrayTypeToClass.get(t);
            Preconditions.checkNotNull(cls, "Unable to decode protobuf message. No array class found for type: " + t);
        }
        final TeraArray.SerializationHandler handler = Preconditions.checkNotNull(arrayHandlers.get(cls), "Unable to decode protobuf message. No serialization handler found for array class: " + cls.getName());
        return handler.deserialize(data.asReadOnlyByteBuffer());
    }
    
    public static final TeraArray.SerializationHandler<? extends TeraArray> getSerializationHandler(Class<? extends TeraArray> arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The paramter 'arrayClass' musst not be null");
        return arrayHandlers.get(arrayClass);
    }
    
    public static final TeraArray.Factory<? extends TeraArray> getFactory(Class<? extends TeraArray> arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The paramter 'arrayClass' musst not be null");
        return arrayFactories.get(arrayClass);
    }
    
    public static final ChunksProtobuf.Type getProtobufType(Class<? extends TeraArray> arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The paramter 'arrayClass' musst not be null");
        final ChunksProtobuf.Type t = arrayClassToType.get(arrayClass);
        if (t != null)
            return t;
        return ChunksProtobuf.Type.Unknown;
    }
    
    public static final Class<? extends TeraArray> getClassByName(String name) {
        Preconditions.checkNotNull(name);
        return arrayNameToClass.get(name);
    }
    
    public static final Class<? extends TeraArray> getArrayClass(ChunksProtobuf.Type protobufType) {
        Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null");
        return arrayTypeToClass.get(protobufType);
    }
    
    public static final void register(TeraArray.Factory<? extends TeraArray> factory, TeraArray.SerializationHandler<? extends TeraArray> handler, ChunksProtobuf.Type protobufType) {
        Preconditions.checkNotNull(factory, "The parameter 'factory' must not be null");
        Preconditions.checkNotNull(handler, "The parameter 'handler' must not be null");
        Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null");
        if (protobufType != ChunksProtobuf.Type.Unknown)
            Preconditions.checkArgument(!arrayTypeToClass.containsKey(protobufType), "The supplied protobuf type is already registered: " + protobufType);
        final Class<? extends TeraArray> cls = factory.getArrayClass();
        Preconditions.checkNotNull(cls, "The method TeraArray.Factory<TeraArray>:getArrayClass() of parameter 'factory' must not return null");
        Preconditions.checkArgument(handler.canHandle(cls), "The supplied handler cannot handle the supplied array class: " + cls.getName());
        Preconditions.checkState(!arrayHandlers.containsKey(cls), "There is already a serialization handler for the supplied array class: " + cls.getName());
        Preconditions.checkState(!arrayFactories.containsKey(cls), "There is already a factory for the supplied array class: " + cls.getName());
        arrayHandlers.put(cls, handler);
        arrayFactories.put(cls, factory);
        if (protobufType != ChunksProtobuf.Type.Unknown) {
            arrayClassToType.put(cls, protobufType);
            arrayTypeToClass.put(protobufType, cls);
        } else {
            final String name = cls.getName();
            Preconditions.checkState(!arrayNameToClass.containsKey(name), "There is already a name entry for the supplied array class: " + cls.getName());
            arrayNameToClass.put(name, cls);
        }
    }
}
