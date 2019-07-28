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

import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.TypeInfo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Use TypeHandlerLibrary-TypeHandlerFactory like approach to generate widgets for types
public class TypeWidgetLibrary {
    private final Context context;
    private final ModuleEnvironment environment;

    private final List<TypeWidgetFactory> widgetFactories = new ArrayList<>();

    public TypeWidgetLibrary(Context context) {
        this.context = context;
        this.environment = context.get(ModuleManager.class).getEnvironment();

        addFactoriesForBuiltinTypes();
    }

    private void addFactoriesForBuiltinTypes() {
        addTypeWidgetFactory(new BooleanWidgetFactory());

        addTypeWidgetFactory(new ByteWidgetFactory());
        addTypeWidgetFactory(new ShortWidgetFactory());
        addTypeWidgetFactory(new IntegerWidgetFactory());
        addTypeWidgetFactory(new LongWidgetFactory());
        addTypeWidgetFactory(new FloatWidgetFactory());
        addTypeWidgetFactory(new DoubleWidgetFactory());

        addTypeWidgetFactory(new StringWidgetFactory());

        addTypeWidgetFactory(new EnumWidgetFactory());
        addTypeWidgetFactory(new CollectionWidgetFactory());
        addTypeWidgetFactory(new ArrayWidgetFactory());
    }

    public void addTypeWidgetFactory(TypeWidgetFactory typeWidgetFactory) {
        InjectionHelper.inject(typeWidgetFactory, context);

        widgetFactories.add(typeWidgetFactory);
    }

    public <T> Optional<UIWidget> getWidget(Binding<T> binding, Class<T> type) {
        return getWidget(binding, TypeInfo.of(type));
    }

    public <T> Optional<UIWidget> getWidget(Binding<T> binding, TypeInfo<T> type) {
        // TODO: Explore reversing typeHandlerFactories itself before building object
        for (int i = widgetFactories.size() - 1; i >= 0; i--) {
            TypeWidgetFactory typeWidgetFactory = widgetFactories.get(i);
            Optional<UIWidget> widget = typeWidgetFactory.create(binding, type, this);

            if (widget.isPresent()) {
                return widget;
            }
        }

        return Optional.empty();
    }
}
