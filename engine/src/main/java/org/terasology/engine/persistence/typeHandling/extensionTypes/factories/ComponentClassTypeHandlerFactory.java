// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes.factories;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.persistence.typeHandling.SpecificTypeHandlerFactory;
import org.terasology.engine.persistence.typeHandling.TypeHandler;
import org.terasology.engine.persistence.typeHandling.TypeHandlerContext;
import org.terasology.engine.persistence.typeHandling.extensionTypes.ComponentClassTypeHandler;
import org.terasology.nui.reflection.TypeInfo;

public class ComponentClassTypeHandlerFactory extends SpecificTypeHandlerFactory<Class<? extends Component>> {

    public ComponentClassTypeHandlerFactory() {
        super(new TypeInfo<Class<? extends Component>>() {});
    }

    @Override
    protected TypeHandler<Class<? extends Component>> createHandler(TypeHandlerContext context) {
        return new ComponentClassTypeHandler(context);
    }
}
