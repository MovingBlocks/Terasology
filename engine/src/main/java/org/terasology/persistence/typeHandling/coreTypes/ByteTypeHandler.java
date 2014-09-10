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
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ByteTypeHandler implements TypeHandler<Byte> {

    @Override
    public PersistedData serialize(Byte value, SerializationContext context) {
        if (value != null) {
            return context.create(new byte[]{value});
        }
        return context.createNull();
    }

    @Override
    public Byte deserialize(PersistedData data, DeserializationContext context) {
        if (data.isBytes()) {
            return data.getAsBytes()[0];
        } else if (data.isNumber()) {
            return (byte) data.getAsInteger();
        }

        return null;
    }

    @Override
    public PersistedData serializeCollection(Collection<Byte> value, SerializationContext context) {
        return context.create(Bytes.toArray(value));
    }

    @Override
    public List<Byte> deserializeCollection(PersistedData data, DeserializationContext context) {
        if (data.isBytes()) {
            return Lists.newArrayList(Bytes.asList(data.getAsBytes()));
        }
        return Lists.newArrayList();
    }
}
