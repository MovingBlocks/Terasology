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
package org.terasology.rendering.nui.internal;

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleContext;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactoryRegistry;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.object.SubtypeLayoutBuilder;

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
