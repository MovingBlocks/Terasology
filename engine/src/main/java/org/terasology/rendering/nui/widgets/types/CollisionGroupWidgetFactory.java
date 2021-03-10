// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.types;

import com.google.common.collect.Lists;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.types.RegisterTypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.CollisionGroupManager;
import org.terasology.reflection.TypeInfo;
import org.terasology.engine.registry.In;

import java.util.ArrayList;
import java.util.Optional;

@RegisterTypeWidgetFactory
public class CollisionGroupWidgetFactory implements TypeWidgetFactory {
    @In
    private CollisionGroupManager collisionGroupManager;

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        if (!CollisionGroup.class.equals(type.getRawType())) {
            return Optional.empty();
        }

        TypeWidgetBuilder<CollisionGroup> builder = new CollisionGroupWidgetBuilder(collisionGroupManager);
        return Optional.of((TypeWidgetBuilder<T>) builder);
    }

    private static class CollisionGroupWidgetBuilder implements TypeWidgetBuilder<CollisionGroup> {
        private ArrayList<CollisionGroup> collisionGroups;

        CollisionGroupWidgetBuilder(CollisionGroupManager collisionGroupManager) {
            collisionGroups = Lists.newArrayList(collisionGroupManager.getAllGroups());
        }

        @Override
        public UIWidget build(Binding<CollisionGroup> binding) {
            UIDropdownScrollable<CollisionGroup> dropdown = new UIDropdownScrollable<>();

            dropdown.setOptions(collisionGroups);
            dropdown.bindSelection(binding);
            dropdown.setOptionRenderer(new StringTextRenderer<CollisionGroup>() {
                @Override
                public String getString(CollisionGroup value) {
                    return value.getName();
                }
            });

            return dropdown;
        }
    }
}
