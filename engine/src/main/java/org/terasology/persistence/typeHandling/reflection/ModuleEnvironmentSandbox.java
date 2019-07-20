/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.persistence.typeHandling.reflection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.terasology.engine.SimpleUri;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;

import java.util.Iterator;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;

public class ModuleEnvironmentSandbox implements SerializationSandbox {
    private final ModuleEnvironment moduleEnvironment;

    public ModuleEnvironmentSandbox(ModuleEnvironment moduleEnvironment) {
        this.moduleEnvironment = moduleEnvironment;
    }

    @Override
    public <T> Optional<Class<? extends T>> findSubTypeOf(String subTypeIdentifier, Class<T> clazz) {

        Iterator<Class<? extends T>> possibilities =
            moduleEnvironment
                .getSubtypesOf(clazz, subclass -> doesSubclassMatch(subclass, subTypeIdentifier))
                .iterator();

        if (possibilities.hasNext()) {
            Class<? extends T> possibility = possibilities.next();

            // Multiple possibilities
            if (possibilities.hasNext()) {
                return Optional.empty();
            }

            return Optional.of(possibility);
        }

        // No possibility
        return Optional.empty();
    }

    private boolean doesSubclassMatch(Class<?> subclass, String subTypeIdentifier) {
        if (subclass == null) {
            return false;
        }

        SimpleUri subTypeUri = new SimpleUri(subTypeIdentifier);
        Name subTypeName = subTypeUri.isValid() ? subTypeUri.getObjectName() : new Name(subTypeIdentifier);

        Name providingModule = moduleEnvironment.getModuleProviding(subclass);


        if (subTypeUri.isValid()) {
            if (!subTypeUri.getModuleName().equals(providingModule)) {
                return false;
            }
        }

        return subTypeName.toString().equals((subclass.getName())) || subTypeName.toString().equals((subclass.getSimpleName()));
    }

    @Override
    public <T> String getSubTypeIdentifier(Class<? extends T> subType, Class<T> baseType) {
        SimpleUri subTypeUri = getTypeSimpleUri(subType);

        long subTypesWithSameUri = Streams.stream(moduleEnvironment.getSubtypesOf(baseType))
                                       .map(this::getTypeSimpleUri)
                                       .filter(subTypeUri::equals)
                                       .count();

        Preconditions.checkArgument(subTypesWithSameUri > 0,
            "Subtype was not found in the module environment");

        if (subTypesWithSameUri > 1) {
            // More than one subType with same SimpleUri, use fully qualified name
            return subType.getName();
        }

        return subTypeUri.toString();
    }

    private SimpleUri getTypeSimpleUri(Class<?> type) {
        Name moduleProvidingType = moduleEnvironment.getModuleProviding(type);
        String typeSimpleName = type.getSimpleName();

        return new SimpleUri(moduleProvidingType, typeSimpleName);
    }
}
