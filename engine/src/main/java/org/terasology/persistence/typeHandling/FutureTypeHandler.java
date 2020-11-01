/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling;

import java.util.Optional;

class FutureTypeHandler<T> extends TypeHandler<T> {
    public TypeHandler<T> typeHandler;

    private void throwIfTypeHandlerNull() {
        if (typeHandler == null) {
            throw new SerializationException("Future TypeHandler has not been generated yet");
        }
    }

    @Override
    public PersistedData serialize(T value, PersistedDataSerializer serializer) {
        throwIfTypeHandlerNull();
        return typeHandler.serialize(value, serializer);
    }

    @Override
    protected PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        throwIfTypeHandlerNull();
        return typeHandler.serializeNonNull(value, serializer);
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        throwIfTypeHandlerNull();
        return typeHandler.deserialize(data);
    }
}
