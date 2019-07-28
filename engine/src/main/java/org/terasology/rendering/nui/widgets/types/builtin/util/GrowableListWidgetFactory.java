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
package org.terasology.rendering.nui.widgets.types.builtin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.List;
import java.util.Optional;

public abstract class GrowableListWidgetFactory<C, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrowableListWidgetFactory.class);

    protected Binding<C> binding;
    protected TypeInfo<C> type;
    protected TypeWidgetLibrary library;
    protected TypeInfo<E> elementType;

    public GrowableListWidgetFactory(Binding<C> binding, TypeInfo<C> type, TypeInfo<E> elementType, TypeWidgetLibrary library) {
        this.binding = binding;
        this.type = type;
        this.elementType = elementType;
        this.library = library;
    }

    public UIWidget create() {
        List<E> elementList = getBindingCopy();

        ColumnLayout mainLayout = new ColumnLayout();

        mainLayout.setFillVerticalSpace(false);
        mainLayout.setAutoSizeColumns(false);

        populateCollectionLayout(elementType, elementList, mainLayout);

        return mainLayout;
    }

    private void populateCollectionLayout(
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
                elementType, elementList, i, collectionLayout
            );

            if (!elementLayout.isPresent()) {
                continue;
            }

            collectionLayout.addWidget(elementLayout.get());
        }
    }

    private Optional<UIWidget> createElementLayout(
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
                    updateBindingWithElements(elementList);
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
            updateBindingWithElements(elementList);

            // Re-add all the widgets because element indices may have to be regenerated
            collectionLayout.removeAllWidgets();
            populateCollectionLayout(elementType, elementList, collectionLayout);
        });

        RowLayout elementLayout = new RowLayout();

        elementLayout.addWidget(removeButton, new RowLayoutHint().setUseContentWidth(true));
        elementLayout.addWidget(elementWidget.get(), new RowLayoutHint().setUseContentWidth(false));

        return Optional.of(elementLayout);
    }

    protected abstract void updateBindingWithElements(List<E> elementList);

    protected abstract List<E> getBindingCopy();
}
