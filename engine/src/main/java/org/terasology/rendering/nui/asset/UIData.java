/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.asset;

import org.terasology.assets.AssetData;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.UIWidget;

/**
 * UIData contains a UI widget that has been loaded from a UI asset, ready to be added to the
 *
 */
@API
public class UIData implements AssetData {
    private UIWidget rootWidget;

    public UIData(UIWidget rootWidget) {
        this.rootWidget = rootWidget;
    }

    /**
     * @return The root widget loaded from the UI asset.
     */
    public UIWidget getRootWidget() {
        return rootWidget;
    }
}
