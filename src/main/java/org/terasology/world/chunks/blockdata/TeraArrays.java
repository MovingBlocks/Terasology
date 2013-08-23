/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.chunks.blockdata;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkState;


/**
 * TeraArrays is the central registration point for all the different TeraArray implementations.
 * It is threadsafe and uses a read/write lock to support concurrent read operations.
 * <p/>
 * Serialization and deserialization of TeraArrays into/from protobuf messages is supported through the methods
 * {@code TeraArrays.encode(TeraArray)} and {@code TeraArrays.decode(ChunksProtobuf.TeraArray)}.
 * <p/>
 * Alternative TeraArray implementations can be registered through the method {@code TeraArrays.register(TeraArray.Factory, ChunksProtobuf.Type)}.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 * @todo Future optimization: Implement some caching/cycling mechanism to avoid unnecessary ByteBuffer allocations.
 */
@SuppressWarnings("rawtypes")
public final class TeraArrays {

    private static final TeraArrays INSTANCE = new TeraArrays();

    private final ReadWriteLock lock;
    private final Map<Class, Entry> arrayClasses;
    private final Map<String, Entry> arrayNames;
    private final Map<ChunksProtobuf.Type, Entry> arrayTypes;

    private TeraArrays() {
        lock = new ReentrantReadWriteLock();

        arrayClasses = Maps.newHashMap();
        arrayTypes = Maps.newHashMap();
        arrayNames = Maps.newHashMap();

        lock.writeLock().lock();
        try {
            register(new TeraDenseArray4Bit.Factory(), Type.DenseArray4Bit);
            register(new TeraDenseArray8Bit.Factory(), Type.DenseArray8Bit);
            register(new TeraDenseArray16Bit.Factory(), Type.DenseArray16Bit);
            register(new TeraSparseArray4Bit.Factory(), Type.SparseArray4Bit);
            register(new TeraSparseArray8Bit.Factory(), Type.SparseArray8Bit);
            register(new TeraSparseArray16Bit.Factory(), Type.SparseArray16Bit);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public ChunksProtobuf.TeraArray encode(TeraArray array) {
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        final Entry entry = getEntry(array.getClass());
        if (entry == null) {
            throw new IllegalArgumentException("Unable to encode the supplied array of class: " + array.getClass().getName());
        }
        final ChunksProtobuf.TeraArray.Builder b = ChunksProtobuf.TeraArray.newBuilder();
        final ByteBuffer buf = entry.handler.serialize(array);
        buf.rewind();
        b.setData(ByteString.copyFrom(buf));
        b.setType(entry.protobufType);
        if (entry.protobufType == ChunksProtobuf.Type.Unknown) {
            b.setClassName(entry.arrayClassName);
        }
        return b.build();
    }

    public TeraArray decode(ChunksProtobuf.TeraArray message) {
        Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
        if (!message.hasType()) {
            throw new IllegalArgumentException("Illformed protobuf message. Missing type information.");
        }
        final ChunksProtobuf.Type type = message.getType();
        final Entry entry;
        if (type == ChunksProtobuf.Type.Unknown) {
            if (!message.hasClassName()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing class name.");
            }
            entry = getEntry(message.getClassName());
            if (entry == null) {
                throw new IllegalArgumentException("Unable to decode protobuf message. No entry found for class name: " + message.getClassName());
            }
        } else {
            entry = getEntry(type);
            if (entry == null) {
                throw new IllegalArgumentException("Unable to decode protobuf message. No entry found for type: " + type);
            }
        }
        if (!message.hasData()) {
            throw new IllegalArgumentException("Illformed protobuf message. Missing byte sequence.");
        }
        final ByteString data = message.getData();
        return entry.handler.deserialize(data.asReadOnlyByteBuffer());
    }

    public Entry getEntry(Class arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The parameter 'arrayClass' must not be null");
        lock.readLock().lock();
        try {
            return arrayClasses.get(arrayClass);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Entry getEntry(ChunksProtobuf.Type protobufType) {
        Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null");
        lock.readLock().lock();
        try {
            return arrayTypes.get(protobufType);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Entry getEntry(String arrayClassName) {
        Preconditions.checkNotNull(arrayClassName, "The parameter 'arrayClassName' must not be null");
        lock.readLock().lock();
        try {
            return arrayNames.get(arrayClassName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Entry[] getCoreArrayEntries() {
        lock.readLock().lock();
        try {
            return arrayTypes.values().toArray(new Entry[arrayTypes.size()]);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Entry[] getArrayEntries() {
        lock.readLock().lock();
        try {
            return arrayNames.values().toArray(new Entry[arrayNames.size()]);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Class[] getArrayClasses() {
        lock.readLock().lock();
        try {
            return arrayClasses.keySet().toArray(new Class[arrayClasses.size()]);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String[] getArrayClassNames() {
        lock.readLock().lock();
        try {
            return arrayNames.keySet().toArray(new String[arrayNames.size()]);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ChunksProtobuf.Type[] getProtobufTypes() {
        lock.readLock().lock();
        try {
            return arrayTypes.keySet().toArray(new ChunksProtobuf.Type[arrayTypes.size()]);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void register(TeraArray.Factory factory, ChunksProtobuf.Type protobufType) {
        lock.writeLock().lock();
        try {
            final Entry entry = new Entry(factory, protobufType);
            checkState(!arrayClasses.containsKey(entry.arrayClass), "There is already a class entry for the supplied array class: " + entry.arrayClassName);
            arrayClasses.put(entry.arrayClass, entry);
            checkState(!arrayNames.containsKey(entry.arrayClassName), "There is already a name entry for the supplied array class: " + entry.arrayClassName);
            arrayNames.put(entry.arrayClassName, entry);
            if (protobufType != ChunksProtobuf.Type.Unknown) {
                checkState(!arrayTypes.containsKey(protobufType), "There is already a protobuf type entry for the supplied array class: " + entry.arrayClassName);
                arrayTypes.put(protobufType, entry);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static TeraArrays getInstance() {
        return INSTANCE;
    }

    public static final class Entry {

        public final Class arrayClass;
        public final String arrayClassName;
        public final TeraArray.Factory factory;
        public final TeraArray.SerializationHandler handler;
        public final ChunksProtobuf.Type protobufType;

        @SuppressWarnings("unchecked")
        private Entry(TeraArray.Factory factory, ChunksProtobuf.Type protobufType) {
            this.factory = Preconditions.checkNotNull(factory, "The parameter 'factory' must not be null");
            this.arrayClass = Preconditions.checkNotNull(factory.getArrayClass());
            this.arrayClassName = arrayClass.getName();
            this.handler = Preconditions.checkNotNull(factory.createSerializationHandler());
            checkState(handler.canHandle(arrayClass));
            this.protobufType = Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null");
        }
    }
}
