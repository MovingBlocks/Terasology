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
package org.terasology.rendering.nui.widgets.types.internal;

import org.terasology.module.Module;
import org.terasology.engine.module.ModuleContext;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

public class ModuleTypeWidgetLibraryWrapper implements TypeWidgetLibrary {
    private final TypeWidgetLibrary delegate;

    private final Module executingModule;

    public ModuleTypeWidgetLibraryWrapper(TypeWidgetLibrary delegate, Module executingModule) {
        this.delegate = delegate;
        this.executingModule = executingModule;
    }

    @Override
    public void addTypeWidgetFactory(TypeWidgetFactory factory) {
        delegate.addTypeWidgetFactory(factory);
    }

    @Override
    public <T> Optional<UIWidget> getWidget(Binding<T> binding, TypeInfo<T> type) {
        try (ModuleContext.ContextSpan ignored = ModuleContext.setContext(executingModule)) {
            return delegate.getWidget(binding, type);
        }
    }
}
