// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.types.object;

import com.google.common.base.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.databinding.NotifyingBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.widgets.UIBox;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.nui.widgets.types.builtin.util.ExpandableLayoutBuilder;
import org.terasology.nui.widgets.types.builtin.util.FieldsWidgetBuilder;
import org.terasology.reflection.TypeInfo;
import org.terasology.engine.utilities.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.terasology.nui.widgets.types.TypeWidgetFactory.LABEL_WIDGET_ID;

public class ObjectLayoutBuilder<T> extends ExpandableLayoutBuilder<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectLayoutBuilder.class);
    private static final String NULL_LABEL = "Object is null.";
    private static final String MODIFY_LABEL = "Modify Object";

    private final TypeInfo<T> type;
    private final TypeWidgetLibrary library;
    private final FieldsWidgetBuilder<T> fieldsWidgetBuilder;

    public ObjectLayoutBuilder(TypeInfo<T> type, TypeWidgetLibrary library) {
        this.type = type;
        this.library = library;
        fieldsWidgetBuilder = new FieldsWidgetBuilder<>(type, library);
    }

    @Override
    protected void populate(Binding<T> binding, ColumnLayout layout, ColumnLayout mainLayout) {
        layout.removeAllWidgets();

        UILabel nameWidget = mainLayout.find(LABEL_WIDGET_ID, UILabel.class);
        assert nameWidget != null;

        if (binding.get() == null) {
            populateNullLayout(binding, layout, nameWidget);
        } else {
            buildEditorLayout(binding, layout, nameWidget);
        }
    }

    private void buildEditorLayout(Binding<T> binding, ColumnLayout layout, UILabel nameWidget) {
        // TODO: Translate
        if (NULL_LABEL.equals(nameWidget.getText())) {
            nameWidget.setText(MODIFY_LABEL);
        }

        UIButton setToNull = new UIButton();

        // TODO: Translate
        setToNull.setText("Set to null");
        setToNull.subscribe(widget -> binding.set(null));

        layout.addWidget(setToNull);

        fieldsWidgetBuilder.getFieldWidgets(binding).forEach(layout::addWidget);
    }

    private void populateNullLayout(Binding<T> binding, ColumnLayout layout, UILabel nameWidget) {
        // TODO: Add assign to reference option

        // TODO: Translate
        if (MODIFY_LABEL.equals(nameWidget.getText())) {
            nameWidget.setText(NULL_LABEL);
        }

        List<Constructor<T>> constructors =
            Arrays.stream(type.getRawType().getConstructors())
                .map(constructor -> (Constructor<T>) constructor)
                .collect(Collectors.toList());

        if (constructors.isEmpty()) {
            // TODO: Translate
            UIBox box = buildErrorWidget("No accessible constructors found");

            layout.addWidget(box);

            return;
        }

        ColumnLayout parameterLayout = createDefaultLayout();

        UIButton createInstanceButton = new UIButton();
        // TODO: Translate
        createInstanceButton.setText("Create Instance");

        UIDropdownScrollable<Constructor<T>> constructorSelection =
            new UIDropdownScrollable<>();

        Binding<Constructor<T>> selectedConstructor =
            new NotifyingBinding<Constructor<T>>(constructors.get(0)) {
                @Override
                protected void onSet() {
                    populateConstructorParameters(binding, parameterLayout, createInstanceButton, this);
                }
            };

        constructorSelection.setOptions(constructors);
        constructorSelection.bindSelection(selectedConstructor);

        constructorSelection.setOptionRenderer(new StringTextRenderer<Constructor<T>>() {
            @Override
            public String getString(Constructor<T> value) {
                return ReflectionUtil.resolvedMethodToString(
                    type.getType(),
                    value,
                    true
                );
            }
        });

        // TODO: Translate
        constructorSelection.setTooltip("Select the constructor to use to create the new object");

        layout.addWidget(constructorSelection);
        layout.addWidget(parameterLayout);
        layout.addWidget(createInstanceButton);
    }

    private void populateConstructorParameters(Binding<T> binding,
                                               ColumnLayout parameterLayout,
                                               UIButton createInstanceButton,
                                               Binding<Constructor<T>> selectedConstructor) {
        parameterLayout.removeAllWidgets();

        Parameter[] parameters = selectedConstructor.get().getParameters();

        List<TypeInfo<?>> parameterTypes =
            Arrays.stream(parameters)
                .map(Parameter::getParameterizedType)
                .map(parameterType -> ReflectionUtil.resolveType(type.getType(), parameterType))
                .map(TypeInfo::of)
                .collect(Collectors.toList());

        List<Binding<?>> argumentBindings =
            parameterTypes.stream()
                .map(parameterType -> new DefaultBinding<>(Defaults.defaultValue(parameterType.getRawType())))
                .collect(Collectors.toList());

        createInstanceButton.subscribe(widget -> {
            Object[] arguments = argumentBindings.stream()
                                     .map(Binding::get)
                                     .toArray();

            try {
                binding.set(selectedConstructor.get().newInstance(arguments));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });


        if (argumentBindings.isEmpty()) {
            // TODO: Translate
            parameterLayout.addWidget(new UILabel("Constructor has no parameters"));
            return;
        }

        ColumnLayout parametersExpandableLayout = WidgetUtil.createExpandableLayout(
            // TODO: Translate
            "Constructor Parameters",
            this::createDefaultLayout,
            layout -> {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    TypeInfo<?> parameterType = parameterTypes.get(i);
                    Binding<?> argumentBinding = argumentBindings.get(i);
                    Parameter parameter = parameters[i];

                    Optional<UIWidget> optionalWidget =
                        library.getBaseTypeWidget((Binding) argumentBinding, parameterType);

                    if (!optionalWidget.isPresent()) {
                        LOGGER.atWarn().log("Could not create widget for parameter of type {} of constructor {}",
                                parameter, selectedConstructor.get());
                        continue;
                    }

                    UIWidget widget = optionalWidget.get();
                    String parameterLabelText = ReflectionUtil.typeToString(parameterType.getType(), true);

                    layout.addWidget(WidgetUtil.labelize(widget, parameterLabelText, LABEL_WIDGET_ID));
                }
            },
            this::createDefaultLayout
        );

        parameterLayout.addWidget(parametersExpandableLayout);
    }
}
