/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.protobuf;

import com.google.protobuf.ByteString;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.protobuf.EntityData;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Immortius
 */
public class ProtobufSerializationContext implements SerializationContext {

    private TypeSerializationLibrary library;

    public ProtobufSerializationContext(TypeSerializationLibrary library) {
        this.library = library;
    }

    @Override
    public PersistedData create(String value) {
        return createStrings(Arrays.asList(value));
    }

    @Override
    public PersistedData create(String... values) {
        return createStrings(Arrays.asList(values));
    }

    @Override
    public PersistedData createStrings(Iterable<String> value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addAllString(value).build());
    }

    @Override
    public PersistedData create(float value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addFloat(value).build());
    }

    @Override
    public PersistedData create(float... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (float val : values) {
            builder.addFloat(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(TFloatIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addFloat(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(int value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addInteger(value).build());
    }

    @Override
    public PersistedData create(int... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (int val : values) {
            builder.addInteger(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(TIntIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addInteger(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(long value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addLong(value).build());
    }

    @Override
    public PersistedData create(long... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (long val : values) {
            builder.addLong(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(TLongIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addLong(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(boolean value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addBoolean(value).build());
    }

    @Override
    public PersistedData create(boolean... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (boolean val : values) {
            builder.addBoolean(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(double value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addDouble(value).build());
    }

    @Override
    public PersistedData create(double... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (double val : values) {
            builder.addDouble(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(TDoubleIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addDouble(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(byte[] value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().setBytes(ByteString.copyFrom(value)).build());
    }

    @Override
    public PersistedData create(ByteBuffer value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().setBytes(ByteString.copyFrom(value)).build());
    }

    @Override
    public PersistedData create(PersistedData... data) {
        return create(Arrays.asList(data));
    }

    @Override
    public PersistedData create(Iterable<PersistedData> data) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (PersistedData value : data) {
            builder.addValue(((ProtobufPersistedData) value).getValue());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData create(Map<String, PersistedData> data) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (Map.Entry<String, PersistedData> entry : data.entrySet()) {
            builder.addNameValue(EntityData.NameValue.newBuilder()
                    .setName(entry.getKey())
                    .setValue(((ProtobufPersistedData) entry.getValue()).getValue()).build());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public <T> PersistedData create(T data, Class<T> type) {
        TypeHandler<T> handler = (TypeHandler<T>) library.getHandlerFor(type);
        return handler.serialize(data, this);
    }

    @Override
    public <T> PersistedData create(Collection<T> data, Class<T> type) {
        TypeHandler<T> handler = (TypeHandler<T>) library.getHandlerFor(type);
        return handler.serializeCollection(data, this);

    }

    @Override
    public PersistedData createNull() {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().build());
    }
}
