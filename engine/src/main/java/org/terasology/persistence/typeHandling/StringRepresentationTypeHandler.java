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
package org.terasology.persistence.typeHandling;

import java.util.Optional;

/**
 */
public abstract class StringRepresentationTypeHandler<T> extends TypeHandler<T> {

    public abstract String getAsString(T item);

    public abstract T getFromString(String representation);

    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        String stringValue = getAsString(value);
        return serializer.serialize(stringValue);
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (data.isString()) {
            return Optional.ofNullable(getFromString(data.getAsString()));
        }
        return Optional.empty();
    }

}
