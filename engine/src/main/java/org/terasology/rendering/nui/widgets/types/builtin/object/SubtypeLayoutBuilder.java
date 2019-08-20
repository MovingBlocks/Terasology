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
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.NotifyingBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.rendering.nui.widgets.types.builtin.util.ExpandableLayoutBuilder;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SubtypeLayoutBuilder<T> extends ExpandableLayoutBuilder<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectWidgetFactory.class);

    private final TypeInfo<T> baseType;
    private final TypeWidgetLibrary library;

    private final List<TypeInfo<T>> allowedSubtypes;
    private final ModuleManager moduleManager;

    private final ColumnLayout widgetContainer = new ColumnLayout();

    private final Binding<TypeInfo<T>> editingType;

    public SubtypeLayoutBuilder(Binding<T> binding,
                                TypeInfo<T> baseType,
                                TypeWidgetLibrary library,
                                ModuleManager moduleManager,
                                TypeRegistry typeRegistry) {
        super(binding);

        this.library = library;

        this.baseType = baseType;
        this.editingType = new NotifyingBinding<TypeInfo<T>>(baseType) {
            @Override
            protected void onSet() {
                widgetContainer.removeAllWidgets();

                Optional<UIWidget> widget = SubtypeLayoutBuilder.this.library
                                                .getWidget(SubtypeLayoutBuilder.this.binding, get());

                if (!widget.isPresent()) {
                    LOGGER.error("Could not find widget for type {}", get());
                    return;
                }

                widgetContainer.addWidget(widget.get());
            }
        };

        this.moduleManager = moduleManager;

        Module contextModule = ModuleContext.getContext();

        PermissionProvider permissionProvider = moduleManager.getPermissionProvider(contextModule);

        ModuleEnvironment environment = moduleManager.getEnvironment();

        Set<Name> allowedProvidingModules =
            ImmutableSet.<Name>builder()
                .add(contextModule.getId())
                .addAll(environment.getDependencyNamesOf(contextModule.getId()))
                .build();

        List<Class<? extends T>> allowedSubclasses =
            typeRegistry.getSubtypesOf(baseType.getRawType())
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

        allowedSubclasses.add(baseType.getRawType());

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
                    Type parameterized = ReflectionUtil.parameterizeandResolveRawType(baseType.getType(), clazz);
                    return (TypeInfo<T>) TypeInfo.of(parameterized);
                })
                .collect(Collectors.toList());
    }

    private Name getModuleProviding(Class<?> type) {
        if (type.getClassLoader() == null) {
            return null;
        }

        return moduleManager.getEnvironment().getModuleProviding(type);
    }

    public UIWidget getLayout() {
        if (allowedSubtypes.size() <= 1) {
            return widgetContainer;
        }

        return mainLayout;

    }

    @Override
    protected void populate(ColumnLayout layout) {
        if (binding.get() != null && !editingType.get().getRawType().equals(binding.get().getClass())) {
            Type actual = ReflectionUtil.parameterizeandResolveRawType(baseType.getType(), binding.get().getClass());
            editingType.set((TypeInfo<T>) TypeInfo.of(actual));
        }

        if (!allowedSubtypes.contains(editingType.get())) {
            Optional<TypeInfo<T>> closestMatch =
                allowedSubtypes.stream()
                    .filter(subtype -> subtype.getRawType().isAssignableFrom(editingType.get().getRawType()))
                    .findFirst();

            // closestMatch is always present since editingType is guaranteed to be a subtype of T
            assert closestMatch.isPresent();

            editingType.set(closestMatch.get());
        }

        UIDropdownScrollable<TypeInfo<T>> typeSelection = new UIDropdownScrollable<>();

        typeSelection.setOptions(allowedSubtypes);
        typeSelection.bindSelection(editingType);
        typeSelection.setOptionRenderer(new StringTextRenderer<TypeInfo<T>>() {
            @Override
            public String getString(TypeInfo<T> value) {
                return getTypeName(value);
            }
        });

        // TODO: Translate
        typeSelection.setTooltip("Select the type for the new object");

        layout.addWidget(typeSelection);
        layout.addWidget(widgetContainer);
    }

    private String getTypeName(TypeInfo<?> value) {
        return ReflectionUtil.getTypeUri(value.getType(), moduleManager.getEnvironment());
    }
}
