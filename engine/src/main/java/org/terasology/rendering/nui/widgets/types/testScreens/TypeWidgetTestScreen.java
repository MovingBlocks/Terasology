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
package org.terasology.rendering.nui.widgets.types.testScreens;

import org.terasology.reflection.TypeInfo;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
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

    @Override
    public void initialise() {
        mainContainer = find("mainContainer", ColumnLayout.class);
        assert mainContainer != null;

        mainContainer.setAutoSizeColumns(true);
        mainContainer.setFillVerticalSpace(false);
        mainContainer.setVerticalSpacing(5);

        addWidgets();

        UIText bindingsLog = new UIText();

        bindingsLog.setReadOnly(true);
        bindingsLog.setMultiline(true);

        bindingsLog.setText(String.join("", Collections.nCopies(bindings.size() - 1, "\n")));

        UIButton logBindingsButton = new UIButton();

        logBindingsButton.setText("Print Binding Values");

        logBindingsButton.subscribe(widget -> {
            String logs = bindings.entrySet()
                              .stream()
                              .map(
                                  binding ->
                                      MessageFormat.format(
                                          "{0} binding has a value {1} of type {2}",
                                          binding.getKey().getRawType().getSimpleName(),
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
        });

        mainContainer.addWidget(bindingsLog);
        mainContainer.addWidget(logBindingsButton);
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
        Binding<T> binding = new DefaultBinding<>();

        bindings.put(type, binding);

        RowLayout row = new RowLayout();

        row.addWidget(
            new UILabel(type.getRawType().getSimpleName()),
            new RowLayoutHint().setRelativeWidth(0.35f)
        );

        row.addWidget(
            typeWidgetLibrary.getWidget(binding, type).get(),
            null
        );

        mainContainer.addWidget(row);
    }

}
