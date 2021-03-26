// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.skin;

import org.terasology.assets.ResourceUrn;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UIStyleFamily;

import java.util.HashMap;
import java.util.Map;

public class UISkinWithUrn extends UISkin {
    private final ResourceUrn urn;

    public UISkinWithUrn(Map<String, UIStyleFamily> skinFamilies, ResourceUrn urn) {
        super(skinFamilies);
        this.urn = urn;
    }

    public static UISkinWithUrn createFromSkin(UISkin skin, ResourceUrn urn) {
        Map<String, UIStyleFamily> skinFamilies = new HashMap<>();
        for (String family : skin.getFamilies()) {
            skinFamilies.put(family, skin.getFamily(family));
        }

        return new UISkinWithUrn(skinFamilies, urn);
    }

    public ResourceUrn getUrn() {
        return urn;
    }
}
