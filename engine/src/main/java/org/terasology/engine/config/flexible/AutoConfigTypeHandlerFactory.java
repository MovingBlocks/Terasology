// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible;

import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

public class AutoConfigTypeHandlerFactory implements TypeHandlerFactory {

    private final TypeHandlerLibrary typeHandlerLibrary;

    public AutoConfigTypeHandlerFactory(TypeHandlerLibrary typeHandlerLibrary) {
        this.typeHandlerLibrary = typeHandlerLibrary;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        if (AutoConfig.class.isAssignableFrom(typeInfo.getRawType())) {
            return Optional.of(new AutoConfigTypeHandler(typeInfo, typeHandlerLibrary));
        }
        return Optional.empty();
    }
}
