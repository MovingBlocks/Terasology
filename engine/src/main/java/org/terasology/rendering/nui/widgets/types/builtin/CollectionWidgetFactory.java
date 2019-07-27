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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.MultiRowLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.utilities.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public class CollectionWidgetFactory implements TypeWidgetFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionWidgetFactory.class);

    private ConstructorLibrary constructorLibrary = new ConstructorLibrary();

    @Override
    public <T> Optional<UIWidget> create(Binding<T> binding, TypeInfo<T> type, TypeWidgetLibrary library) {
        Class<T> rawType = type.getRawType();

        if (!Collection.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }

        if (binding.get() == null) {
            ObjectConstructor<T> constructor = constructorLibrary.get(type);
            assert constructor != null;

            binding.set(constructor.construct());
        }

        return Optional.of(createWidget((Binding<Collection<Object>>) binding, (TypeInfo<Collection<Object>>) type, library));
    }

    private <T extends Collection<E>, E> ColumnLayout createWidget(
        Binding<T> binding,
        TypeInfo<T> type,
        TypeWidgetLibrary library
    ) {
        @SuppressWarnings({"unchecked"})
        TypeInfo<E> elementType = (TypeInfo<E>) TypeInfo.of(
            ReflectionUtil.getTypeParameterForSuper(type.getType(), Collection.class, 0)
        );

        List<E> elementList = new ArrayList<>(binding.get());

        ColumnLayout mainLayout = new ColumnLayout();

        mainLayout.setFillVerticalSpace(false);
        mainLayout.setAutoSizeColumns(false);

        populateCollectionLayout(binding, type, library, elementType, elementList, mainLayout);

        return mainLayout;
    }

    private <T extends Collection<E>, E> void populateCollectionLayout(
        Binding<T> binding,
        TypeInfo<T> type,
        TypeWidgetLibrary library,
        TypeInfo<E> elementType,
        List<E> elementList,
        ColumnLayout collectionLayout
    ) {
        UIButton addElementButton = new UIButton();

        // TODO: Translate
        addElementButton.setText("Add Element");
        addElementButton.subscribe(widget -> {
            elementList.add(null);

            Optional<UIWidget> elementLayout = createElementLayout(
                binding, type, library,
                elementType, elementList, elementList.size() - 1, collectionLayout
            );

            if (!elementLayout.isPresent()) {
                return;
            }

            collectionLayout.addWidget(elementLayout.get());
        });

        collectionLayout.addWidget(addElementButton);

        for (int i = 0; i < elementList.size(); i++) {
            Optional<UIWidget> elementLayout = createElementLayout(
                binding, type, library,
                elementType, elementList, i, collectionLayout
            );

            if (!elementLayout.isPresent()) {
                continue;
            }

            collectionLayout.addWidget(elementLayout.get());
        }
    }

    private <T extends Collection<E>, E> Optional<UIWidget> createElementLayout(
        Binding<T> binding,
        TypeInfo<T> type,
        TypeWidgetLibrary library,
        TypeInfo<E> elementType,
        List<E> elementList,
        int elementIndex,
        ColumnLayout collectionLayout
    ) {
        Optional<UIWidget> elementWidget = library.getWidget(
            new Binding<E>() {
                @Override
                public E get() {
                    return elementList.get(elementIndex);
                }

                @Override
                public void set(E value) {
                    elementList.set(elementIndex, value);
                    updateBindingWith(binding, type, elementList);
                }
            },
            elementType
        );

        if (!elementWidget.isPresent()) {
            LOGGER.error(
                "Could not get widget for element {} in collection",
                elementList.get(elementIndex)
            );
            return Optional.empty();
        }

        UIButton removeButton = new UIButton();
        // TODO: Translate
        removeButton.setText("-");

        removeButton.subscribe(widget -> {
            elementList.remove(elementIndex);
            updateBindingWith(binding, type, elementList);

            collectionLayout.removeAllWidgets();
            populateCollectionLayout(binding, type, library, elementType, elementList, collectionLayout);
        });

        RowLayout elementLayout = new RowLayout();

        elementLayout.addWidget(removeButton, new RowLayoutHint().setUseContentWidth(true));
        elementLayout.addWidget(elementWidget.get(), new RowLayoutHint().setUseContentWidth(false));

        return Optional.of(elementLayout);
    }

    private <T extends Collection<E>, E> void updateBindingWith(Binding<T> binding, TypeInfo<T> type, Collection<E> items) {
        try {
            binding.get().clear();
            binding.get().addAll(items);
        } catch (UnsupportedOperationException e) {
            // Bound collection is unmodifiable, create new
            binding.set(newImmutableCollection(type, items));
        }
    }

    private <T extends Collection<E>, E> T newImmutableCollection(TypeInfo<T> type, Collection<E> items) {
        Class<T> rawType = type.getRawType();

        // If the bound collection is unmodifiable, it must either be a standard
        // Collection or a guava ImmutableCollection, so casts always succeed

        // TODO: Support more Guava types?

        if (SortedSet.class.isAssignableFrom(rawType)) {
            return (T) ImmutableSortedSet.copyOf(items);
        }

        if (Set.class.isAssignableFrom(rawType)) {
            return (T) ImmutableSet.copyOf(items);
        }

        return (T) ImmutableList.copyOf(items);
    }
}
