// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.entitySystem.Component;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;

public class ComponentClassTypeHandler extends StringRepresentationTypeHandler<Class<? extends Component>> {

    TypeHandlerContext context;

    public ComponentClassTypeHandler(final TypeHandlerContext context) {
        this.context = context;
    }

    @Override
    public String getAsString(Class<? extends Component> item) {
        return context.getSandbox().getSubTypeIdentifier(item, Component.class);
    }

    @Override
    public Class<? extends Component> getFromString(String representation) {
        return context.getSandbox().findSubTypeOf(representation, Component.class).orElse(null);
    }
}
