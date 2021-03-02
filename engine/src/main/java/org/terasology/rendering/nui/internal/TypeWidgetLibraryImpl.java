// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.internal;

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleContext;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetFactoryRegistry;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;
import org.terasology.rendering.nui.widgets.types.object.SubtypeLayoutBuilder;

import java.util.Optional;

public class TypeWidgetLibraryImpl implements TypeWidgetLibrary {
    private final TypeWidgetFactoryRegistry widgetFactories;
    private final Module contextModule;
    private final Context context;

    public TypeWidgetLibraryImpl(TypeWidgetFactoryRegistry widgetFactories,
                                 Module contextModule,
                                 Context context) {
        this.widgetFactories = widgetFactories;
        this.contextModule = contextModule;
        this.context = context;
    }

    @Override
    public <T> Optional<UIWidget> getWidget(Binding<T> binding, TypeInfo<T> type) {
        return getBuilder(type).map(builder -> builder.build(binding));
    }

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> getBuilder(TypeInfo<T> type) {
        try(ModuleContext.ContextSpan ignored = ModuleContext.setContext(contextModule)) {

            // Iterate in reverse order so that later added factories take priority
            for (TypeWidgetFactory factory : Lists.reverse(widgetFactories.getFactories())) {
                Optional<TypeWidgetBuilder<T>> widget = factory.create(type, this);

                if (widget.isPresent()) {
                    return widget;
                }
            }

            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<UIWidget> getWidget(Binding<T> binding, Class<T> type) {
        return getWidget(binding, TypeInfo.of(type));
    }

    @Override
    public <T> Optional<UIWidget> getBaseTypeWidget(Binding<T> binding, TypeInfo<T> baseType) {
        try(ModuleContext.ContextSpan ignored = ModuleContext.setContext(contextModule)) {
            if (Primitives.isWrapperType(baseType.getRawType()) || baseType.getRawType().isPrimitive()) {
                return getWidget(binding, baseType);
            }

            ModuleManager moduleManager = context.get(ModuleManager.class);
            TypeRegistry typeRegistry = context.get(TypeRegistry.class);

            SubtypeLayoutBuilder<T> builder = new SubtypeLayoutBuilder<>(
                baseType, this, moduleManager, typeRegistry
            );

            return Optional.of(builder.build(binding));
        }
    }
}
