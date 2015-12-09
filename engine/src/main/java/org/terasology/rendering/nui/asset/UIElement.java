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
package org.terasology.rendering.nui.asset;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.UIWidget;

/**
 */
@API
public class UIElement extends Asset<UIData> {

    private UIWidget rootWidget;

    private final List<Consumer<UIElement>> reloadListeners = new CopyOnWriteArrayList<>();

    public UIElement(ResourceUrn urn, AssetType<?, UIData> assetType, UIData data) {
        super(urn, assetType);
        reload(data);
    }

    /**
     * Subscribe to reload events.
     * @param reloadListener the listener to add
     */
    public void subscribe(Consumer<UIElement> reloadListener) {
        reloadListeners.add(reloadListener);
    }

    /**
     * Unsubscribe from reload events.
     * @param reloadListener the listener to remove. Non-existing entries will be ignored.
     */
    public void unsubscribe(Consumer<UIElement> reloadListener) {
        reloadListeners.remove(reloadListener);
    }

    @Override
    protected void doReload(UIData data) {
        rootWidget = data.getRootWidget();
        for (Consumer<UIElement> listener : reloadListeners) {
            listener.accept(this);
        }
    }

    public UIWidget getRootWidget() {
        return rootWidget;
    }
}
