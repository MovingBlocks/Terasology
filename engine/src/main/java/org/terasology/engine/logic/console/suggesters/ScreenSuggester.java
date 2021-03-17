// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import org.terasology.assets.management.AssetManager;
import org.terasology.nui.asset.UIElement;

public final class ScreenSuggester extends AssetSuggester {
    public ScreenSuggester(AssetManager assetManager) {
        super(UIElement.class, assetManager);
    }
}
