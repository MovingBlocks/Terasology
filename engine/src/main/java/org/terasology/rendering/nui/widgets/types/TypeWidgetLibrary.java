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

import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;

import java.util.Optional;

/**
 * Creates {@link UIWidget UIWidgets} to edit objects of various types.
 *
 * Instances can only be accessed via injection (see {@link org.terasology.registry.In}) in
 * screens and overlays.
 */
public interface TypeWidgetLibrary {
    void addTypeWidgetFactory(TypeWidgetFactory typeWidgetFactory);

    <T> Optional<UIWidget> getWidget(Binding<T> binding, TypeInfo<T> type);

    <T> Optional<UIWidget> getWidget(Binding<T> binding, Class<T> type);
}
