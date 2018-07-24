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

import java.util.List;

/**
 */
public interface DeserializationContext {

    /**
     * Attempts to deserialize the given persisted data as the specified type. Type handlers should take care not to invoke this on the type they handle or otherwise in
     * a recursive manner.
     *
     * @param data The persisted data to deserialize
     * @param type The type of the deserialized object.
     * @param <T> The type of the deserialized object.
     * @return An object of type
     * @throws org.terasology.persistence.typeHandling.DeserializationException if the data cannot be deserialized as type.
     */
    <T> T deserializeAs(PersistedData data, Class<T> type);
}
