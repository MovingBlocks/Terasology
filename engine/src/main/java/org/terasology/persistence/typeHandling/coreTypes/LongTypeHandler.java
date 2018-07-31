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

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

/**
 */
public class LongTypeHandler extends TypeHandler<Long> {

    @Override
    public PersistedData serializeNonNull(Long value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Long deserialize(PersistedData data) {
        if (data.isNumber()) {
            return data.getAsLong();
        }
        return null;
    }

}
