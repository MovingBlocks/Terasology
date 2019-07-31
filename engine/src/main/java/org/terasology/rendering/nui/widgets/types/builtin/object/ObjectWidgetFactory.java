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
package org.terasology.rendering.nui.widgets.types.builtin.object;

import org.terasology.engine.module.ModuleManager;
import org.terasology.reflection.TypeInfo;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.lang.reflect.Modifier;
import java.util.Optional;

public class ObjectWidgetFactory implements TypeWidgetFactory {
    @In
    private ModuleManager moduleManager;

    @Override
    public <T> Optional<UIWidget> create(Binding<T> binding, TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        // If the class is a local class or a non-static member class, we don't want to handle it
        if (rawType.isLocalClass() ||
                (rawType.isMemberClass() && !Modifier.isStatic(rawType.getModifiers()))) {
            return Optional.empty();
        }

        ObjectLayoutBuilder<T> layoutBuilder = new ObjectLayoutBuilder<>(binding, type, library, moduleManager);
        layoutBuilder.build();

        return Optional.of(layoutBuilder.getLayout());
    }

}
