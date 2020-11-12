// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.testScreens;

import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.InteriorMutationNotifyingBinding;
import org.terasology.nui.databinding.NotifyingBinding;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.widgets.UIText;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.reflection.TypeInfo;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.utilities.ReflectionUtil;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class TypeWidgetTestScreen extends CoreScreenLayer {
    private ColumnLayout mainContainer;

    @In
    private TypeWidgetLibrary typeWidgetLibrary;

    private Map<TypeInfo<?>, Binding<?>> bindings = new LinkedHashMap<>();
    private UIText bindingsLog;

    @Override
    public void initialise() {
        mainContainer = find("mainContainer", ColumnLayout.class);
        assert mainContainer != null;

        mainContainer.setAutoSizeColumns(true);
        mainContainer.setFillVerticalSpace(false);
        mainContainer.setVerticalSpacing(5);

        bindingsLog = new UIText();

        bindingsLog.setReadOnly(true);
        bindingsLog.setMultiline(true);

        addWidgets();

        mainContainer.addWidget(bindingsLog);
    }

    private void dumpBindings() {
        String logs = bindings.entrySet()
                          .stream()
                          .map(
                              binding ->
                                  MessageFormat.format(
                                      "{0} binding has a value {1} of type {2}",
                                      typeInfoToString(binding.getKey()),
                                      toString(binding.getValue().get()),
                                      Objects.toString(
                                          Optional.ofNullable(binding.getValue().get())
                                              .map(val -> val.getClass().getSimpleName())
                                              .orElse(null)
                                      )
                                  )
                          )
                          .collect(Collectors.joining("\n"));

        bindingsLog.setText(logs);
    }

    public String toString(Object object) {
        if (object != null && object.getClass().isArray()) {
            return Arrays.toString((Object[]) object);
        }

        return Objects.toString(object);
    }

    protected abstract void addWidgets();

    protected <T> void newBinding(Class<T> type) {
        newBinding(TypeInfo.of(type));
    }

    protected <T> void newBinding(TypeInfo<T> type) {
        Binding<T> binding = new InteriorMutationNotifyingBinding<>(
            new NotifyingBinding<T>() {
                @Override
                protected void onSet() {
                    dumpBindings();
                }
            },
            this::dumpBindings
        );

        bindings.put(type, binding);

        UIWidget bindingWidget = typeWidgetLibrary.getWidget(binding, type).get();
        String bindingLabelText = typeInfoToString(type);

        mainContainer.addWidget(WidgetUtil.labelize(bindingWidget, bindingLabelText, TypeWidgetFactory.LABEL_WIDGET_ID));
    }

    private <T> String typeInfoToString(TypeInfo<T> type) {
        return ReflectionUtil.typeToString(type.getType(), true);
    }

}
