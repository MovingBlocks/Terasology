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
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

/**
 * Class containing static factory methods for generating {@link GsonBuilder} objects that follow Terasology
 * serialization rules and support Terasology TypeHandlers.
 */
public class GsonBuilderFactory {
    /**
     * Create a {@link GsonBuilder} with options set to comply with Terasology JSON serialization rules.
     */
    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder()
                .setExclusionStrategies(new GsonMapExclusionStrategy());
    }

    /**
     * Create a {@link GsonBuilder} which uses type handlers loaded from the given
     * {@link TypeHandlerLibrary} and complies with Terasology JSON serialization rules.
     *
     * @param typeHandlerLibrary The {@link TypeHandlerLibrary} to load type handler
     *                                 definitions from
     */
    public static GsonBuilder createGsonBuilderWithTypeSerializationLibrary(TypeHandlerLibrary typeHandlerLibrary) {
        TypeAdapterFactory typeAdapterFactory =
                new GsonTypeSerializationLibraryAdapterFactory(typeHandlerLibrary);

        return createDefaultGsonBuilder()
                .registerTypeAdapterFactory(typeAdapterFactory);
    }

    /**
     * Create a {@link GsonBuilder} which uses the given type handlers and complies with Terasology
     * JSON serialization rules.
     *
     * @param typeHandlerEntries The type handlers to use during serialization.
     */
    @SuppressWarnings("unchecked")
    public static GsonBuilder createGsonBuilderWithTypeHandlers(TypeHandlerEntry<?>... typeHandlerEntries) {
        GsonTypeHandlerAdapterFactory typeAdapterFactory = new GsonTypeHandlerAdapterFactory();

        for (TypeHandlerEntry typeHandlerEntry : typeHandlerEntries) {
            typeAdapterFactory.addTypeHandler(typeHandlerEntry);
        }

        return createDefaultGsonBuilder()
                .registerTypeAdapterFactory(typeAdapterFactory);
    }
}
