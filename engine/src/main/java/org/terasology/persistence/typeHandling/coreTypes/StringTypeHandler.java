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
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
public class StringTypeHandler implements TypeHandler<String> {

    @Override
    public PersistedData serialize(String value, SerializationContext context) {
        return context.create(value);
    }

    @Override
    public String deserialize(PersistedData data, DeserializationContext context) {
        return data.getAsString();
    }

    @Override
    public PersistedData serializeCollection(Collection<String> value, SerializationContext context) {
        return context.createStrings(value);

    }

    @Override
    public List<String> deserializeCollection(PersistedData data, DeserializationContext context) {
        return Lists.newArrayList(data.getAsArray().getAsStringArray());
    }
}
