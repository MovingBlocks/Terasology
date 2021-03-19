// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.protobuf;

import com.google.protobuf.ByteString;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.protobuf.EntityData;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 */
public class ProtobufPersistedDataSerializer implements PersistedDataSerializer {

    @Override
    public PersistedData serialize(String value) {
        return serializeStrings(Arrays.asList(value));
    }

    @Override
    public PersistedData serialize(String... values) {
        return serializeStrings(Arrays.asList(values));
    }

    @Override
    public PersistedData serializeStrings(Iterable<String> value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addAllString(value).build());
    }

    @Override
    public PersistedData serialize(float value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addFloat(value).build());
    }

    @Override
    public PersistedData serialize(float... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (float val : values) {
            builder.addFloat(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(TFloatIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addFloat(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(int value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addInteger(value).build());
    }

    @Override
    public PersistedData serialize(int... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (int val : values) {
            builder.addInteger(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(TIntIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addInteger(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(long value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addLong(value).build());
    }

    @Override
    public PersistedData serialize(long... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (long val : values) {
            builder.addLong(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(TLongIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addLong(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(boolean value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addBoolean(value).build());
    }

    @Override
    public PersistedData serialize(boolean... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (boolean val : values) {
            builder.addBoolean(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(double value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().addDouble(value).build());
    }

    @Override
    public PersistedData serialize(double... values) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (double val : values) {
            builder.addDouble(val);
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(TDoubleIterator value) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        while (value.hasNext()) {
            builder.addDouble(value.next());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(byte[] value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().setBytes(ByteString.copyFrom(value)).build());
    }

    @Override
    public PersistedData serialize(ByteBuffer value) {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().setBytes(ByteString.copyFrom(value)).build());
    }

    @Override
    public PersistedData serialize(PersistedData... data) {
        return serialize(Arrays.asList(data));
    }

    @Override
    public PersistedData serialize(Iterable<PersistedData> data) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (PersistedData value : data) {
            builder.addValue(((ProtobufPersistedData) value).getValue());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serialize(Map<String, PersistedData> data) {
        EntityData.Value.Builder builder = EntityData.Value.newBuilder();
        for (Map.Entry<String, PersistedData> entry : data.entrySet()) {
            builder.addNameValue(EntityData.NameValue.newBuilder()
                    .setName(entry.getKey())
                    .setValue(((ProtobufPersistedData) entry.getValue()).getValue()).build());
        }
        return new ProtobufPersistedData(builder.build());
    }

    @Override
    public PersistedData serializeNull() {
        return new ProtobufPersistedData(EntityData.Value.newBuilder().build());
    }
}
