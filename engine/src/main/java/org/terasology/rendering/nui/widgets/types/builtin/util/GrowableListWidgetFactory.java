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
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISpace;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.utilities.ReflectionUtil;

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

    private static ColumnLayout createDefaultLayout() {
        ColumnLayout mainLayout = new ColumnLayout();

        mainLayout.setFillVerticalSpace(false);
        mainLayout.setAutoSizeColumns(false);
        mainLayout.setVerticalSpacing(5);

        return mainLayout;
    }

    public UIWidget create() {
        List<E> elementList = getBindingCopy();

        String labelText = "Edit " + ReflectionUtil.typeToString(type.getType(), true);
        UILabel labelWidget = new UILabel(TypeWidgetFactory.LABEL_WIDGET_ID, labelText);

        return WidgetUtil.createExpandableLayout(
            labelWidget,
            GrowableListWidgetFactory::createDefaultLayout,
            collectionLayout -> populateCollectionLayout(elementList, collectionLayout),
            GrowableListWidgetFactory::createDefaultLayout
        );
    }

    private void populateCollectionLayout(
        List<E> elementList,
        ColumnLayout collectionLayout
    ) {
        for (int i = 0; i < elementList.size(); i++) {
            Optional<UIWidget> elementLayout = createElementLayout(
                elementList, i, collectionLayout
            );

            if (!elementLayout.isPresent()) {
                continue;
            }

            collectionLayout.addWidget(elementLayout.get());
        }

        UIButton addElementButton = new UIButton();

        // TODO: Translate
        addElementButton.setText("Add Element");
        addElementButton.subscribe(widget -> {
            elementList.add(null);
            // We won't update the binding just yet since we want an actual value, not null
            // Some collections like guava's ImmutableCollection don't even allow null elements

            collectionLayout.removeAllWidgets();
            populateCollectionLayout(elementList, collectionLayout);
        });

        collectionLayout.addWidget(addElementButton);
    }

    private Optional<UIWidget> createElementLayout(
        List<E> elementList,
        int elementIndex,
        ColumnLayout collectionLayout
    ) {
        Optional<UIWidget> optionalElementWidget = library.getWidget(
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

        if (!optionalElementWidget.isPresent()) {
            LOGGER.error(
                "Could not get widget for element {} in collection",
                elementList.get(elementIndex)
            );
            return Optional.empty();
        }

        UIWidget elementWidget = optionalElementWidget.get();

        UIButton removeButton = new UIButton();
        // TODO: Translate
        removeButton.setText("Remove");

        removeButton.subscribe(widget -> {
            elementList.remove(elementIndex);
            updateBindingWithElements(elementList);

            // Re-add all the widgets because element indices may have to be regenerated
            collectionLayout.removeAllWidgets();
            populateCollectionLayout(elementList, collectionLayout);
        });

        // TODO: Translate
        String elementLabelText = "Element " + elementIndex;

        RowLayout elementLayout = new RowLayout();
        elementLayout.setHorizontalSpacing(5);

        ColumnLayout removeButtonLayout = new ColumnLayout();

        removeButtonLayout.addWidget(removeButton);

        // Add space to ensure that button does not stretch vertically
        removeButtonLayout.addWidget(new UISpace());

        removeButtonLayout.setExtendLast(true);
        removeButtonLayout.setAutoSizeColumns(true);

        elementLayout.addWidget(removeButtonLayout, new RowLayoutHint().setUseContentWidth(true));

        elementLayout.addWidget(
            WidgetUtil.labelize(elementWidget, elementLabelText, TypeWidgetFactory.LABEL_WIDGET_ID),
            new RowLayoutHint()
        );

        return Optional.of(elementLayout);
    }

    protected abstract void updateBindingWithElements(List<E> elementList);

    protected abstract List<E> getBindingCopy();
}
