// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.nui.skin.UISkinAsset;

public final class SkinSuggester extends AssetSuggester {
    public SkinSuggester(AssetManager assetManager) {
        super(UISkinAsset.class, assetManager);
    }
}

