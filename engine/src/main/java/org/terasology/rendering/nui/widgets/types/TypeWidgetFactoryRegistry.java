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

import com.google.common.collect.Maps;
import org.terasology.context.Context;
import org.terasology.persistence.typeHandling.InstanceCreator;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.registry.InjectionHelper;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Registers {@link TypeWidgetFactory} instances that can be used by a {@link TypeWidgetLibrary}
 * to generate widgets to edit objects of various types.
 */
public class TypeWidgetFactoryRegistry {
    private final Context context;
    private final List<TypeWidgetFactory> factories = new ArrayList<>();

    private final Map<Type, InstanceCreator<?>> instanceCreators = Maps.newHashMap();
    private final ConstructorLibrary constructorLibrary = new ConstructorLibrary(instanceCreators);

    public TypeWidgetFactoryRegistry(Context context) {
        this.context = context;
        addFactoriesForBuiltinTypes();
    }

    private void addFactoriesForBuiltinTypes() {
        add(new ObjectWidgetFactory());

        add(new BooleanWidgetFactory());

        add(new ByteWidgetFactory());
        add(new ShortWidgetFactory());
        add(new IntegerWidgetFactory());
        add(new LongWidgetFactory());
        add(new FloatWidgetFactory());
        add(new DoubleWidgetFactory());

        add(new StringWidgetFactory());

        add(new EnumWidgetFactory());
        add(new CollectionWidgetFactory(constructorLibrary));
        add(new ArrayWidgetFactory(constructorLibrary));
    }

    public void add(TypeWidgetFactory factory) {
        InjectionHelper.inject(factory, context);
        factories.add(factory);
    }

    public List<TypeWidgetFactory> getFactories() {
        return Collections.unmodifiableList(factories);
    }
}
