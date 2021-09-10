// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.serializers;

import org.terasology.persistence.typeHandling.PersistedData;

import java.io.IOException;
import java.io.InputStream;

/**
 * Read {@link PersistedData} from files, stream, buffer, etc.
 */
public interface PersistedDataReader<T extends PersistedData> {
    T read(InputStream inputStream) throws IOException;

    T read(byte[] byteBuffer) throws IOException;
}
