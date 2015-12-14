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

import com.google.common.collect.Lists;

import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

import java.util.List;

/**
 */
public class ProtobufDeserializationContext implements DeserializationContext {

    private TypeSerializationLibrary typeSerializationLibrary;

    public ProtobufDeserializationContext(TypeSerializationLibrary typeSerializationLibrary) {
        this.typeSerializationLibrary = typeSerializationLibrary;
    }

    @Override
    public <T> T deserializeAs(PersistedData data, Class<T> type) {
        TypeHandler<?> handler = typeSerializationLibrary.getHandlerFor(type);
        if (handler == null) {
            throw new DeserializationException("No handler found for " + type);
        }
        return type.cast(handler.deserialize(data, this));
    }

    @Override
    public <T> List<T> deserializeCollection(PersistedData data, Class<T> type) {
        if (!data.isArray()) {
            throw new IllegalStateException("Data is not an array");
        }
        List<T> result = Lists.newArrayList();
        for (PersistedData item : data.getAsArray()) {
            result.add(deserializeAs(item, type));
        }
        return result;
    }
}
