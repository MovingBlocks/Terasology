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
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;

public class ModuleEnvironmentSandbox implements SerializationSandbox {
    private final ModuleManager moduleManager;

    public ModuleEnvironmentSandbox(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private ModuleEnvironment getModuleEnvironment() {
        return moduleManager.getEnvironment();
    }

    @Override
    public <T> Optional<Class<? extends T>> findSubTypeOf(String subTypeIdentifier, Class<T> clazz) {

        Iterator<Class<? extends T>> possibilities =
            getModuleEnvironment()
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

        // First check full name
        boolean fullNameEquals = subTypeName.toString().equals((subclass.getName()));

        if (fullNameEquals) {
            return true;
        }

        // Now check through module and simple name
        Name providingModule = getModuleEnvironment().getModuleProviding(subclass);
        Name givenModuleName;

        if (subTypeUri.isValid()) {
            givenModuleName = subTypeUri.getModuleName();
        } else {
            // Assume that the requested subtype is in the context module
            givenModuleName = ModuleContext.getContext() != null ? ModuleContext.getContext().getId() : null;
        }

        return Objects.equals(givenModuleName, providingModule) && subTypeName.toString().equals(subclass.getSimpleName());
    }

    @Override
    public <T> String getSubTypeIdentifier(Class<? extends T> subType, Class<T> baseType) {
        SimpleUri subTypeUri = getTypeSimpleUri(subType);

        long subTypesWithSameUri = Streams.stream(getModuleEnvironment().getSubtypesOf(baseType))
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

    @Override
    public <T> boolean isValidTypeHandlerDeclaration(TypeInfo<T> type, TypeHandler<T> typeHandler) {
        Name moduleDeclaringHandler = getModuleEnvironment().getModuleProviding(typeHandler.getClass());

        // If handler was declared outside of a module (engine or somewhere else), we allow it
        // TODO: Possibly find better way to refer to engine module
        if (moduleDeclaringHandler == null || moduleDeclaringHandler.equals(new Name("engine"))) {
            return true;
        }

        // Handler has been declared in a module, proceed accordingly

        if (type.getRawType().getClassLoader() == null) {
            // Modules cannot specify handlers for builtin classes
            return false;
        }

        Name moduleDeclaringType = getModuleEnvironment().getModuleProviding(type.getRawType());

        // Both the type and the handler must come from the same module
        return Objects.equals(moduleDeclaringType, moduleDeclaringHandler);
    }

    private SimpleUri getTypeSimpleUri(Class<?> type) {
        Name moduleProvidingType = getModuleEnvironment().getModuleProviding(type);
        String typeSimpleName = type.getSimpleName();

        return new SimpleUri(moduleProvidingType, typeSimpleName);
    }
}
