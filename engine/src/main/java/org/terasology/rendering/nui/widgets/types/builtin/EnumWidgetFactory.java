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
package org.terasology.rendering.nui.widgets.types.builtin;

import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.Arrays;
import java.util.Optional;

public class EnumWidgetFactory implements TypeWidgetFactory {
    @In
    private TranslationSystem translationSystem;

    @Override
    public <T> Optional<UIWidget> create(Binding<T> binding, Class<T> type, TypeWidgetLibrary library) {
        if (!type.isEnum()) {
            return Optional.empty();
        }

        UIDropdownScrollable<T> dropdown = new UIDropdownScrollable<>();

        dropdown.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
        dropdown.setOptions(Arrays.asList(type.getEnumConstants()));

        dropdown.bindSelection(binding);

        return Optional.of(dropdown);
    }

}
