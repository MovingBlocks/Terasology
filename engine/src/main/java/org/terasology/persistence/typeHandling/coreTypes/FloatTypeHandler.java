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
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

/**
 */
public class FloatTypeHandler implements TypeHandler<Float> {

    @Override
    public PersistedData serialize(Float value, SerializationContext context) {
        if (value != null) {
            return context.create(value);
        }
        return context.createNull();
    }

    @Override
    public Float deserialize(PersistedData data) {
        if (data.isNumber()) {
            return data.getAsFloat();
        }
        return null;
    }

}
