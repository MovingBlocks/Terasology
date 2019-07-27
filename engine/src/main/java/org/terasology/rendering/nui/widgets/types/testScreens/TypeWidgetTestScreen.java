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

import org.terasology.context.Context;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.layouts.miglayout.MigLayout;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TypeWidgetTestScreen extends CoreScreenLayer {
    @In
    private Context context;

    private MigLayout mainContainer;
    private TypeWidgetLibrary typeWidgetLibrary;

    private Map<Class<?>, Binding<?>> bindings = new LinkedHashMap<>();

    @Override
    public void initialise() {
        typeWidgetLibrary = new TypeWidgetLibrary(context);

        mainContainer = find("mainContainer", MigLayout.class);
        assert mainContainer != null;

        mainContainer.setRowConstraints("[min!]");

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
                                           "{0} Binding has a value: {1}",
                                           binding.getKey().getSimpleName(),
                                           binding.getValue().get().toString()
                                       )
                              )
                              .collect(Collectors.joining("\n"));

            bindingsLog.setText(logs);
        });

        mainContainer.addWidget(
            bindingsLog,
            new MigLayout.CCHint("newline, spanx 2, gaptop 10mm")
        );

        mainContainer.addWidget(
            logBindingsButton,
            new MigLayout.CCHint("newline, spanx 2")
        );
    }

    protected abstract void addWidgets();

    protected <T> void newBinding(Class<T> type) {
        Binding<T> binding = new DefaultBinding<>();

        bindings.put(type, binding);

        mainContainer.addWidget(
            new UILabel(type.getSimpleName()),
            new MigLayout.CCHint("newline")
        );

        mainContainer.addWidget(
            typeWidgetLibrary.getWidget(binding, type).get(),
            new MigLayout.CCHint("")
        );
    }

}
