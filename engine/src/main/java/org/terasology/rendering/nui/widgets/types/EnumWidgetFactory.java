// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.types;

import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.itemRendering.ToStringTextRenderer;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.reflection.TypeInfo;
import org.terasology.engine.registry.In;

import java.util.Arrays;
import java.util.Optional;

public class EnumWidgetFactory implements TypeWidgetFactory {
    @In
    private TranslationSystem translationSystem;

    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        if (!rawType.isEnum()) {
            return Optional.empty();
        }

        return Optional.of(binding -> createDropdown(binding, rawType));
    }

    private <T> UIDropdownScrollable<T> createDropdown(Binding<T> binding, Class<T> rawType) {
        UIDropdownScrollable<T> dropdown = new UIDropdownScrollable<>();

        dropdown.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
        dropdown.setOptions(Arrays.asList(rawType.getEnumConstants()));

        dropdown.bindSelection(binding);
        return dropdown;
    }

}
