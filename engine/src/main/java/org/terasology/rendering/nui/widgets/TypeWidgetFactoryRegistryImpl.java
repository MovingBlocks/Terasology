// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets;

import com.google.common.collect.Maps;
import org.terasology.context.Context;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetFactoryRegistry;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.nui.widgets.types.builtin.BooleanWidgetFactory;
import org.terasology.nui.widgets.types.builtin.ByteWidgetFactory;
import org.terasology.nui.widgets.types.builtin.DoubleWidgetFactory;
import org.terasology.nui.widgets.types.builtin.FloatWidgetFactory;
import org.terasology.nui.widgets.types.builtin.IntegerWidgetFactory;
import org.terasology.nui.widgets.types.builtin.LongWidgetFactory;
import org.terasology.nui.widgets.types.builtin.ShortWidgetFactory;
import org.terasology.nui.widgets.types.builtin.StringWidgetFactory;
import org.terasology.persistence.typeHandling.InstanceCreator;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.widgets.types.ArrayWidgetFactory;
import org.terasology.rendering.nui.widgets.types.CollectionWidgetFactory;
import org.terasology.rendering.nui.widgets.types.EnumWidgetFactory;
import org.terasology.rendering.nui.widgets.types.object.ObjectWidgetFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Registers {@link TypeWidgetFactory} instances that can be used by a {@link TypeWidgetLibrary}
 * to generate widgets to edit objects of various types.
 */
public class TypeWidgetFactoryRegistryImpl implements TypeWidgetFactoryRegistry {
    private final Context context;
    private final List<TypeWidgetFactory> factories = new ArrayList<>();

    private final Map<Type, InstanceCreator<?>> instanceCreators = Maps.newHashMap();
    private final ConstructorLibrary constructorLibrary = new ConstructorLibrary(instanceCreators);

    public TypeWidgetFactoryRegistryImpl(Context context) {
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

    @Override
    public void add(TypeWidgetFactory factory) {
        InjectionHelper.inject(factory, context);
        factories.add(factory);
    }

    @Override
    public List<TypeWidgetFactory> getFactories() {
        return Collections.unmodifiableList(factories);
    }
}
