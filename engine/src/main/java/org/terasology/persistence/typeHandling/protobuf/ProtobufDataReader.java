// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import org.terasology.persistence.serializers.PersistedDataReader;
import org.terasology.protobuf.EntityData;

import java.io.IOException;
import java.io.InputStream;

public class ProtobufDataReader implements PersistedDataReader<ProtobufPersistedData> {
    @Override
    public ProtobufPersistedData read(InputStream inputStream) throws IOException {
        EntityData.Value value = EntityData.Value.parseDelimitedFrom(inputStream);
        return new ProtobufPersistedData(value);
    }

    @Override
    public ProtobufPersistedData read(byte[] byteBuffer) throws InvalidProtocolBufferException {
        EntityData.Value value = EntityData.Value.parseFrom(byteBuffer);
        return new ProtobufPersistedData(value);
    }
}
