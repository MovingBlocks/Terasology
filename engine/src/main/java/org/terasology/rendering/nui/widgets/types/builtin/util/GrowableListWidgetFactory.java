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
import org.terasology.rendering.nui.databinding.NotifyingBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISpace;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.utilities.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.terasology.rendering.nui.widgets.types.TypeWidgetFactory.LABEL_WIDGET_ID;

public abstract class GrowableListWidgetFactory<C, E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrowableListWidgetFactory.class);

    protected Binding<C> binding;
    protected TypeInfo<C> type;
    protected TypeWidgetLibrary library;
    protected TypeInfo<E> elementType;

    private List<Binding<E>> elements;
    private List<Optional<UIWidget>> elementLayouts;

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
        elements = getBindingStream()
                          .map(this::getBindingForElement)
                          .collect(Collectors.toList());

        String labelText = "Edit " + ReflectionUtil.typeToString(type.getType(), true);
        UILabel labelWidget = new UILabel(LABEL_WIDGET_ID, labelText);

        ColumnLayout collectionLayout = createDefaultLayout();
        elementLayouts = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++) {
            Optional<UIWidget> elementLayout = createElementLayout(
                i, collectionLayout
            );

            elementLayouts.add(elementLayout);
        }

        return WidgetUtil.createExpandableLayout(
            labelWidget,
            () -> collectionLayout,
            this::populateCollectionLayout,
            GrowableListWidgetFactory::createDefaultLayout
        );
    }

    private Binding<E> getBindingForElement(E element) {
        return new NotifyingBinding<E>(element) {
            @Override
            protected void onSet() {
                updateBinding();
            }
        };
    }

    private void populateCollectionLayout(ColumnLayout collectionLayout) {
        for (Optional<UIWidget> elementWidget : elementLayouts) {
            elementWidget.ifPresent(collectionLayout::addWidget);
        }

        UIButton addElementButton = new UIButton();

        // TODO: Translate
        addElementButton.setText("Add Element");
        addElementButton.subscribe(widget -> {
            elements.add(getBindingForElement(null));
            updateBinding();

            Optional<UIWidget> elementLayout = createElementLayout(elements.size() - 1, collectionLayout);
            elementLayouts.add(elementLayout);
            elementLayout.ifPresent(collectionLayout::addWidget);
        });

        collectionLayout.addWidget(addElementButton);
    }

    private void updateElementLabels() {
        for (int i = 0; i < elementLayouts.size(); i++) {
            Optional<UIWidget> optionalElementLayout = elementLayouts.get(i);

            if (!optionalElementLayout.isPresent()) {
                continue;
            }

            UIWidget elementLayout = optionalElementLayout.get();

            // This should never be null, since each element layout is labelized
            UILabel label = elementLayout.find(LABEL_WIDGET_ID, UILabel.class);
            label.setText(getElementLabelText(i));
        }
    }

    private Optional<UIWidget> createElementLayout(int elementIndex, ColumnLayout collectionLayout) {
        Optional<UIWidget> optionalElementWidget = library.getWidget(
            elements.get(elementIndex),
            elementType
        );

        if (!optionalElementWidget.isPresent()) {
            LOGGER.error(
                "Could not get widget for element {} in collection",
                elements.get(elementIndex)
            );
            return Optional.empty();
        }

        UIWidget elementWidget = optionalElementWidget.get();

        UIButton removeButton = new UIButton();
        // TODO: Translate
        removeButton.setText("Remove");

        removeButton.subscribe(widget -> {
            elements.remove(elementIndex);
            updateBinding();

            Optional<UIWidget> elementLayout = elementLayouts.remove(elementIndex);
            elementLayout.ifPresent(collectionLayout::removeWidget);

            updateElementLabels();
        });

        // TODO: Translate
        String elementLabelText = getElementLabelText(elementIndex);

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
            WidgetUtil.labelize(elementWidget, elementLabelText, LABEL_WIDGET_ID),
            new RowLayoutHint()
        );

        return Optional.of(elementLayout);
    }

    private String getElementLabelText(int index) {
        return "Element " + index;
    }

    private void updateBinding() {
        updateBindingWithElements(elements.stream().map(Binding::get).collect(Collectors.toList()));
    }

    protected abstract void updateBindingWithElements(List<E> elements);

    protected abstract Stream<E> getBindingStream();
}
