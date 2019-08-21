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
package org.terasology.rendering.nui.widgets.types.custom;

import com.google.common.collect.Lists;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.reflection.TypeInfo;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.types.RegisterTypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

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
