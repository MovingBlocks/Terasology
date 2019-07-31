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
package org.terasology.rendering.nui.widgets.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleContext;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.InstanceCreator;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.builtin.ArrayWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.BooleanWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.ByteWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.CollectionWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.DoubleWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.EnumWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.FloatWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.IntegerWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.LongWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.ShortWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.StringWidgetFactory;
import org.terasology.rendering.nui.widgets.types.builtin.object.ObjectWidgetFactory;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * Creates {@link UIWidget UIWidgets} to edit objects of various types.
 *
 * Instances can only be accessed via injection (see {@link org.terasology.registry.In}) in
 * screens and overlays.
 */
public class TypeWidgetLibrary {
    private final Map<Type, InstanceCreator<?>> instanceCreators = Maps.newHashMap();
    private final ConstructorLibrary constructorLibrary = new ConstructorLibrary(instanceCreators);

    private final TypeWidgetFactoryRegistry widgetFactories;
    private final Module contextModule;

    public TypeWidgetLibrary(TypeWidgetFactoryRegistry widgetFactories,
                             Module contextModule) {
        this.widgetFactories = widgetFactories;
        this.contextModule = contextModule;

        addFactoriesForBuiltinTypes();
    }

    private void addFactoriesForBuiltinTypes() {
        addTypeWidgetFactory(new ObjectWidgetFactory());

        addTypeWidgetFactory(new BooleanWidgetFactory());

        addTypeWidgetFactory(new ByteWidgetFactory());
        addTypeWidgetFactory(new ShortWidgetFactory());
        addTypeWidgetFactory(new IntegerWidgetFactory());
        addTypeWidgetFactory(new LongWidgetFactory());
        addTypeWidgetFactory(new FloatWidgetFactory());
        addTypeWidgetFactory(new DoubleWidgetFactory());

        addTypeWidgetFactory(new StringWidgetFactory());

        addTypeWidgetFactory(new EnumWidgetFactory());
        addTypeWidgetFactory(new CollectionWidgetFactory(constructorLibrary));
        addTypeWidgetFactory(new ArrayWidgetFactory(constructorLibrary));
    }

    public void addTypeWidgetFactory(TypeWidgetFactory typeWidgetFactory) {
        widgetFactories.add(typeWidgetFactory);
    }

    public <T> Optional<UIWidget> getWidget(Binding<T> binding, TypeInfo<T> type) {
        try(ModuleContext.ContextSpan ignored = ModuleContext.setContext(contextModule)) {

            // Iterate in reverse order so that later added factories take priority
            for (TypeWidgetFactory factory : Lists.reverse(widgetFactories.getFactories())) {
                Optional<UIWidget> widget = factory.create(binding, type, this);

                if (widget.isPresent()) {
                    return widget;
                }
            }

            return Optional.empty();
        }
    }

    public <T> Optional<UIWidget> getWidget(Binding<T> binding, Class<T> type) {
        return getWidget(binding, TypeInfo.of(type));
    }
}
