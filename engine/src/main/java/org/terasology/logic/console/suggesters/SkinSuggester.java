// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import org.terasology.assets.management.AssetManager;
import org.terasology.nui.skin.UISkin;

public final class SkinSuggester extends AssetSuggester {
    public SkinSuggester(AssetManager assetManager) {
        super(UISkin.class, assetManager);
    }
}

