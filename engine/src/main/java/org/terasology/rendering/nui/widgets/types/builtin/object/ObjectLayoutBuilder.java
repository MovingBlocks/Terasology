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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleContext;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.NotifyingBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class ObjectLayoutBuilder<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectWidgetFactory.class);

    private final Binding<T> binding;
    private final TypeInfo<? extends T> type;
    private final TypeWidgetLibrary library;

    private final ColumnLayout mainLayout;

    private final List<TypeInfo<? extends T>> allowedSubtypes;
    private final ModuleManager moduleManager;

    private TypeInfo<? extends T> editingType;

    public ObjectLayoutBuilder(Binding<T> binding,
                               TypeInfo<T> type,
                               TypeWidgetLibrary library,
                               ModuleManager moduleManager) {
        this.type = type;
        this.editingType = type;
        this.library = library;

        this.binding = new NotifyingBinding<T>(binding) {
            @Override
            protected void onSet() {
                build();
            }
        };

        this.moduleManager = moduleManager;

        mainLayout = createDefaultLayout();

        final Name contextModule = ModuleContext.getContext().getId();

        ModuleEnvironment environment = moduleManager.getEnvironment();

        final Set<Name> allowedProvidingModules =
            ImmutableSet.<Name>builder()
                .add(contextModule)
                .addAll(environment.getDependencyNamesOf(contextModule))
                .build();

        List<Class<? extends T>> allowedSubclasses =
            Streams.stream(environment.getSubtypesOf(type.getRawType()))
                .filter(clazz -> allowedProvidingModules.contains(environment.getModuleProviding(clazz)))
                // Filter public, instantiable types
                .filter(clazz -> {
                    int modifiers = clazz.getModifiers();
                    return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && !clazz.isInterface();
                })
                // Filter non-local, static inner classes
                .filter(clazz -> {
                    if (clazz.isLocalClass()) {
                        return false;
                    }
                    return !clazz.isMemberClass() || Modifier.isStatic(clazz.getModifiers());
                })
                .collect(Collectors.toList());

        allowedSubclasses.add(type.getRawType());

        // Sort the subclasses so that the ones at the bottom of the inheritance tree are
        // near the beginning -- useful when finding the closest parent in inheritance tree
        for (int i = 0; i < allowedSubclasses.size() - 1; i++) {
            Class<? extends T> a = allowedSubclasses.get(i);
            for (int j = i + 1; j < allowedSubclasses.size(); j++) {
                Class<? extends T> b = allowedSubclasses.get(j);

                if (!a.isAssignableFrom(b)) {
                    continue;
                }

                allowedSubclasses.set(i, b);
                allowedSubclasses.set(j, a);
            }
        }

        allowedSubtypes =
            allowedSubclasses
                .stream()
                .map(clazz -> {
                    Type parameterized = ReflectionUtil.parameterizeandResolveRawType(type.getType(), clazz);
                    return (TypeInfo<? extends T>) TypeInfo.of(parameterized);
                })
                .collect(Collectors.toList());
    }

    private static ColumnLayout createDefaultLayout() {
        ColumnLayout layout = new ColumnLayout();

        layout.setFillVerticalSpace(false);
        layout.setAutoSizeColumns(false);
        layout.setVerticalSpacing(5);

        return layout;
    }

    public UIWidget getLayout() {
        return mainLayout;
    }

    public void build() {
        mainLayout.removeAllWidgets();

        if (binding.get() == null) {
            buildNullLayout();
        } else {
            buildEditorLayout();
        }
    }

    public void buildNullLayout() {

        // TODO: Add assign to reference option

        // TODO: Translate
        UILabel nameWidget = new UILabel(TypeWidgetFactory.LABEL_WIDGET_ID, "Object is null.");

        ColumnLayout instantiatorLayout =  WidgetUtil.createExpandableLayout(
            nameWidget,
            ObjectLayoutBuilder::createDefaultLayout,
            this::populateInstantiatorLayout,
            ObjectLayoutBuilder::createDefaultLayout
        );

        mainLayout.addWidget(instantiatorLayout);
    }

    private void populateInstantiatorLayout(ColumnLayout instantiatorLayout) {
        if (allowedSubtypes.isEmpty()) {
            // TODO: Translate
            UIBox box = buildErrorWidget("No accessible subtypes found");

            instantiatorLayout.addWidget(box);

            return;
        }

        ColumnLayout constructorLayout = createDefaultLayout();

        Binding<TypeInfo<? extends T>> selectedType =
            new NotifyingBinding<TypeInfo<? extends T>>(allowedSubtypes.get(0)) {
                @Override
                protected void onSet() {
                    // TODO: Check if we already can create a UIWidget for the selected type
                    populateConstructorLayout(constructorLayout, this);
                }
            };

        UIDropdownScrollable<TypeInfo<? extends T>> typeSelection = new UIDropdownScrollable<>();

        typeSelection.setOptions(allowedSubtypes);
        typeSelection.bindSelection(selectedType);
        typeSelection.setOptionRenderer(new StringTextRenderer<TypeInfo<? extends T>>() {
            @Override
            public String getString(TypeInfo<? extends T> value) {
                return getTypeName(value);
            }
        });

        // TODO: Translate
        typeSelection.setTooltip("Select the type for the new object");

        instantiatorLayout.addWidget(typeSelection);
        instantiatorLayout.addWidget(constructorLayout);
    }

    public String getTypeName(TypeInfo<? extends T> value) {
        return ReflectionUtil.simpleUriOfType(value.getType(), moduleManager.getEnvironment()).toString();
    }

    private UIBox buildErrorWidget(String errorMessage) {
        UIBox box = new UIBox();

        // TODO: Translate
        box.setContent(new UILabel(errorMessage + ", cannot instantiate object from UI"));
        return box;
    }

    private void populateConstructorLayout(ColumnLayout constructorLayout,
                                           Binding<TypeInfo<? extends T>> selectedType) {
        constructorLayout.removeAllWidgets();

        List<Constructor<T>> constructors =
            Arrays.stream(selectedType.get().getRawType().getConstructors())
                .map(constructor -> (Constructor<T>) constructor)
                .collect(Collectors.toList());

        if (constructors.isEmpty()) {
            // TODO: Translate
            UIBox box = buildErrorWidget("No accessible constructors found");

            constructorLayout.addWidget(box);

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
                    populateConstructorParameters(parameterLayout, createInstanceButton,
                        selectedType, this);
                }
            };

        constructorSelection.setOptions(constructors);
        constructorSelection.bindSelection(selectedConstructor);

        constructorSelection.setOptionRenderer(new StringTextRenderer<Constructor<T>>() {
            @Override
            public String getString(Constructor<T> value) {
                return ReflectionUtil.resolvedMethodToString(
                    selectedType.get().getType(),
                    value,
                    true
                );
            }
        });

        // TODO: Translate
        constructorSelection.setTooltip("Select the constructor to use to create the new object");

        constructorLayout.addWidget(constructorSelection);
        constructorLayout.addWidget(parameterLayout);
        constructorLayout.addWidget(createInstanceButton);

        populateConstructorParameters(parameterLayout, createInstanceButton,
            selectedType, selectedConstructor);
    }

    private void populateConstructorParameters(ColumnLayout parameterLayout,
                                               UIButton createInstanceButton,
                                               Binding<TypeInfo<? extends T>> selectedType,
                                               Binding<Constructor<T>> selectedConstructor) {
        parameterLayout.removeAllWidgets();

        Type[] parameterTypes = selectedConstructor.get().getGenericParameterTypes();

        String labelText;
        if (parameterTypes.length == 0) {
            // TODO: Translate
            labelText = "Constructor has no parameters";
        } else {
            // TODO: Translate
            labelText = "Set Constructor Parameters:";
        }

        parameterLayout.addWidget(new UILabel(labelText));

        List<Binding<?>> argumentBindings =
            Arrays.stream(parameterTypes)
                .map(parameterType -> {
                    Type resolvedParameterType =
                        ReflectionUtil.resolveType(selectedType.get().getType(), parameterType);

                    DefaultBinding<?> parameterBinding = new DefaultBinding<>();

                    Optional<UIWidget> widget =
                        library.getWidget((Binding) parameterBinding, TypeInfo.of(resolvedParameterType));

                    // TODO: Handle Optional.empty
                    parameterLayout.addWidget(widget.get());

                    return parameterBinding;
                })
                .collect(Collectors.toList());

        createInstanceButton.subscribe(widget -> {
            Object[] arguments = argumentBindings.stream()
                                     .map(Binding::get)
                                     .toArray();

            editingType = selectedType.get();

            try {
                binding.set(selectedConstructor.get().newInstance(arguments));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void buildEditorLayout() {
        if (!editingType.getRawType().equals(binding.get().getClass())) {
            Type actual = ReflectionUtil.parameterizeandResolveRawType(type.getType(), binding.get().getClass());
            editingType = (TypeInfo<? extends T>) TypeInfo.of(actual);
        }

        if (!allowedSubtypes.contains(editingType)) {
            Optional<TypeInfo<? extends T>> closestMatch =
                allowedSubtypes.stream()
                    .filter(subtype -> subtype.getRawType().isAssignableFrom(editingType.getRawType()))
                    .findFirst();

            // closestMatch is always present since editingType is guaranteed to be a subtype of T
            assert closestMatch.isPresent();

            editingType = closestMatch.get();
        }

        // TODO: Translate
        UILabel nameWidget = new UILabel(TypeWidgetFactory.LABEL_WIDGET_ID, "Edit Object of type " + getTypeName(editingType));

        ColumnLayout expandableFieldsLayout = WidgetUtil.createExpandableLayout(
            nameWidget,
            ObjectLayoutBuilder::createDefaultLayout,
            this::populateFieldsLayout,
            ObjectLayoutBuilder::createDefaultLayout
        );

        mainLayout.addWidget(expandableFieldsLayout);
    }

    private void populateFieldsLayout(ColumnLayout fieldsLayout) {
        UIButton setToNull = new UIButton();

        // TODO: Translate
        setToNull.setText("Set to null");
        setToNull.subscribe(widget -> binding.set(null));

        fieldsLayout.addWidget(setToNull);

        for (Field field : ReflectionUtils.getAllFields(editingType.getRawType())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Type resolvedFieldType = ReflectionUtil.resolveType(editingType.getType(), field.getGenericType());
            Optional<UIWidget> optionalFieldWidget = getFieldWidget(field, TypeInfo.of(resolvedFieldType));

            if (!optionalFieldWidget.isPresent()) {
                continue;
            }

            UIWidget fieldWidget = optionalFieldWidget.get();
            // TODO: Translate
            String fieldLabel = "Field '" + field.getName() + "'";

            Optional<UILabel> fieldLabelWidget = fieldWidget.tryFind(TypeWidgetFactory.LABEL_WIDGET_ID, UILabel.class);

            if (fieldLabelWidget.isPresent()) {
                fieldLabelWidget.get().setText(fieldLabel);

                fieldsLayout.addWidget(fieldWidget);
            } else {
                RowLayout fieldLayout = new RowLayout();

                fieldLayout.addWidget(new UILabel(fieldLabel), new RowLayoutHint().setUseContentWidth(true));
                fieldLayout.addWidget(fieldWidget, new RowLayoutHint());

                fieldsLayout.addWidget(fieldLayout);
            }
        }
    }

    private <F> Optional<UIWidget> getFieldWidget(Field field, TypeInfo<F> fieldType) {
        Optional<Binding<F>> fieldBinding = getFieldBinding(field, fieldType);

        if (!fieldBinding.isPresent()) {
            return Optional.empty();
        }

        Optional<UIWidget> widget = library.getWidget(fieldBinding.get(), fieldType);

        if (!widget.isPresent()) {
            LOGGER.warn("Could not create a UIWidget for field {}", field);
            return Optional.empty();
        }

        return widget;
    }

    private <F> Optional<Binding<F>> getFieldBinding(Field field, TypeInfo<F> fieldType) {
        if (Modifier.isPublic(field.getModifiers())) {
            return Optional.of(
                getAccessibleFieldBinding(field, fieldType)
            );
        }

        return getPropertyBinding(field, fieldType);
    }

    private <F> Optional<Binding<F>> getPropertyBinding(Field field, TypeInfo<F> fieldType) {
        Method setter = ReflectionUtil.findSetter(field);

        if (setter == null) {
            return Optional.empty();
        }

        Method getter = ReflectionUtil.findGetter(field);

        if (getter == null) {
            LOGGER.error("Cannot edit field {} with setter {} but no getter", field, setter);
            return Optional.empty();
        }

        return Optional.of(
            new Binding<F>() {
                @Override
                public F get() {
                    try {
                        return fieldType.getRawType().cast(getter.invoke(binding.get()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unreachable");
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e.getCause());
                    }
                }

                @Override
                public void set(F value) {
                    try {
                        setter.invoke(binding.get(), value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unreachable");
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e.getCause());
                    }
                }
            }
        );
    }

    private <F> Binding<F> getAccessibleFieldBinding(Field field, TypeInfo<F> fieldType) {
        return new Binding<F>() {
            @Override
            public F get() {
                try {
                    return fieldType.getRawType().cast(field.get(binding.get()));
                } catch (IllegalAccessException e) {
                    // Field is public
                    throw new RuntimeException("Unreachable");
                }
            }

            @Override
            public void set(F value) {
                try {
                    field.set(binding.get(), value);
                } catch (IllegalAccessException e) {
                    // Field is public
                    throw new RuntimeException("Unreachable");
                }
            }
        };
    }

}
