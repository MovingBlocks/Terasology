// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

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
