// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.protobuf;

import com.google.protobuf.CodedOutputStream;
import org.terasology.persistence.serializers.PersistedDataWriter;

import java.io.IOException;
import java.io.OutputStream;

public class ProtobufDataWriter implements PersistedDataWriter<ProtobufPersistedData> {
    @Override
    public byte[] writeBytes(ProtobufPersistedData data) {
        return data.getValue().toByteArray();
    }

    @Override
    public void writeTo(ProtobufPersistedData data, OutputStream outputStream) throws IOException {
        data.getValue().writeTo(CodedOutputStream.newInstance(outputStream));
    }
}
