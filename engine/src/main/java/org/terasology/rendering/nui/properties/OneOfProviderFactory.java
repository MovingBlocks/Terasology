/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.properties;

import com.google.common.collect.Maps;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;

import java.util.List;
import java.util.Map;

/**
 */
@API
public class OneOfProviderFactory {
    private Map<String, Binding<?>> provider = Maps.newHashMap();
    private Map<String, ItemRenderer<?>> itemRenderers = Maps.newHashMap();

    public OneOfProviderFactory() {
    }

    public <T> void register(String name, Binding<List<T>> binding) {
        register(name, binding, null);
    }

    public <T> void register(String name, Binding<List<T>> binding, ItemRenderer<T> itemRenderer) {
        provider.put(name, binding);
        itemRenderers.put(name, itemRenderer);
    }

    public Binding<?> get(String name) {
        return provider.get(name);
    }

    public ItemRenderer<?> getItemRenderer(String name) {
        return itemRenderers.get(name);
    }
}
