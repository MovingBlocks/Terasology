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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

public class GsonUtility {
    public static GsonBuilder createDefaultGsonBuilder() {
        return new GsonBuilder()
                .setExclusionStrategies(new GsonMapExclusionStrategy());
    }

    public static Gson createGsonWithTypeSerializationLibrary(TypeSerializationLibrary typeSerializationLibrary) {
        TypeAdapterFactory typeAdapterFactory =
                new GsonTypeSerializationLibraryAdapterFactory(typeSerializationLibrary);

        return createDefaultGsonBuilder()
                .registerTypeAdapterFactory(typeAdapterFactory)
                .create();
    }

    public static Gson createGsonWithTypeHandlers(TypeHandlerEntry<?>... typeHandlerPairs) {
        GsonTypeHandlerAdapterFactory typeAdapterFactory = new GsonTypeHandlerAdapterFactory();

        for (TypeHandlerEntry typeHandlerPair : typeHandlerPairs) {
            typeAdapterFactory.addTypeHandler(typeHandlerPair.type, typeHandlerPair.typeHandler);
        }

        return createDefaultGsonBuilder()
                .registerTypeAdapterFactory(typeAdapterFactory)
                .create();
    }
}
