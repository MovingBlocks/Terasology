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
package org.terasology.rendering.nui.widgets.types;

import org.terasology.context.Context;
import org.terasology.registry.InjectionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registers {@link TypeWidgetFactory} instances that can be used by a {@link TypeWidgetLibrary}
 * to generate widgets to edit objects of various types.
 */
public class TypeWidgetFactoryRegistry {
    private final Context context;
    private final List<TypeWidgetFactory> factories = new ArrayList<>();

    public TypeWidgetFactoryRegistry(Context context) {
        this.context = context;
    }

    public void add(TypeWidgetFactory factory) {
        InjectionHelper.inject(factory, context);
        factories.add(factory);
    }

    public List<TypeWidgetFactory> getFactories() {
        return Collections.unmodifiableList(factories);
    }
}
