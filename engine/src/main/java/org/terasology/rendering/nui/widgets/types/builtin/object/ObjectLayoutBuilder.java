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

import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableSet;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleContext;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.sandbox.PermissionProvider;
import org.terasology.naming.Name;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.NotifyingBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.ObjectWidgetUtil;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.terasology.rendering.nui.widgets.types.TypeWidgetFactory.LABEL_WIDGET_ID;

class ObjectLayoutBuilder<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectWidgetFactory.class);

    private final Binding<T> binding;
    private final TypeInfo<? extends T> type;
    private final TypeWidgetLibrary library;

    private final ColumnLayout mainLayout;
    private final UILabel nameWidget = new UILabel(LABEL_WIDGET_ID, "");
    private final ColumnLayout innerExpandableLayout = createDefaultLayout();

    private final List<TypeInfo<? extends T>> allowedSubtypes;
    private final ModuleManager moduleManager;

    private TypeInfo<? extends T> editingType;

    public ObjectLayoutBuilder(Binding<T> binding,
                               TypeInfo<T> type,
                               TypeWidgetLibrary library,
                               ModuleManager moduleManager,
                               TypeRegistry typeRegistry) {
        this.type = type;
        this.editingType = type;
        this.library = library;

        this.binding = new NotifyingBinding<T>(binding) {
            @Override
            protected void onSet() {
                if (!innerExpandableLayout.iterator().hasNext()) {
                    // If it is empty (collapsed), we don't need to rebuild the inner layout
                    return;
                }

                innerExpandableLayout.removeAllWidgets();
                build(innerExpandableLayout);
            }
        };

        this.moduleManager = moduleManager;

        mainLayout = WidgetUtil.createExpandableLayout(
            nameWidget,
            () -> innerExpandableLayout,
            this::build,
            ObjectLayoutBuilder::createDefaultLayout
        );

        Module contextModule = ModuleContext.getContext();

        PermissionProvider permissionProvider = moduleManager.getPermissionProvider(contextModule);

        ModuleEnvironment environment = moduleManager.getEnvironment();

        Set<Name> allowedProvidingModules =
            ImmutableSet.<Name>builder()
                .add(contextModule.getId())
                .addAll(environment.getDependencyNamesOf(contextModule.getId()))
                .build();

        List<Class<? extends T>> allowedSubclasses =
            typeRegistry.getSubtypesOf(type.getRawType())
                .stream()
                // Type must come from an allowed module or be in the whitelist
                .filter(clazz -> allowedProvidingModules.contains(getModuleProviding(clazz)) ||
                                     permissionProvider.isPermitted(clazz))
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

    private Name getModuleProviding(Class<?> type) {
        if (type.getClassLoader() == null) {
            return null;
        }

        return moduleManager.getEnvironment().getModuleProviding(type);
    }

    public UIWidget getLayout() {
        return mainLayout;
    }

    private void build(ColumnLayout layout) {
        if (binding.get() == null) {
            buildNullLayout(layout);
        } else {
            buildEditorLayout(layout);
        }
    }

    private void buildNullLayout(ColumnLayout instantiatorLayout) {

        // TODO: Add assign to reference option

        // TODO: Translate
        if ("".equals(nameWidget.getText())) {
            nameWidget.setText("Object is null.");
        }

        populateInstantiatorLayout(instantiatorLayout);
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
        return ReflectionUtil.getTypeUri(value.getType(), moduleManager.getEnvironment());
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
    }

    private void populateConstructorParameters(ColumnLayout parameterLayout,
                                               UIButton createInstanceButton,
                                               Binding<TypeInfo<? extends T>> selectedType,
                                               Binding<Constructor<T>> selectedConstructor) {
        parameterLayout.removeAllWidgets();

        Parameter[] parameters = selectedConstructor.get().getParameters();

        List<TypeInfo<?>> parameterTypes =
            Arrays.stream(parameters)
                .map(Parameter::getParameterizedType)
                .map(parameterType -> ReflectionUtil.resolveType(selectedType.get().getType(), parameterType))
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

            editingType = selectedType.get();

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
            ObjectLayoutBuilder::createDefaultLayout,
            layout -> {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    TypeInfo<?> parameterType = parameterTypes.get(i);
                    Binding<?> argumentBinding = argumentBindings.get(i);
                    Parameter parameter = parameters[i];

                    Optional<UIWidget> optionalWidget =
                        library.getWidget((Binding) argumentBinding, parameterType);

                    if (!optionalWidget.isPresent()) {
                        LOGGER.warn("Could not create widget for parameter {} of constructor {}",
                            parameter, selectedConstructor.get());
                        continue;
                    }

                    UIWidget widget = optionalWidget.get();
                    String parameterLabelText = ReflectionUtil.typeToString(parameterType.getType(), true);

                    layout.addWidget(WidgetUtil.labelize(widget, parameterLabelText, LABEL_WIDGET_ID));
                }
            },
            ObjectLayoutBuilder::createDefaultLayout
        );

        parameterLayout.addWidget(parametersExpandableLayout);
    }

    private void buildEditorLayout(ColumnLayout fieldsLayout) {
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
        if ("".equals(nameWidget.getText())) {
            nameWidget.setText("Edit Object of type " + getTypeName(editingType));
        }

        populateFieldsLayout(fieldsLayout);
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
            String fieldLabel = field.getName();

            fieldsLayout.addWidget(WidgetUtil.labelize(fieldWidget, fieldLabel, LABEL_WIDGET_ID));
        }
    }

    private <F> Optional<UIWidget> getFieldWidget(Field field, TypeInfo<F> fieldType) {
        Optional<Binding<F>> fieldBinding = ObjectWidgetUtil.getFieldBinding(binding, field);

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

}
