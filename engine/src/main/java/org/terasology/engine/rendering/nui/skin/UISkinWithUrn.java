// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.skin;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UIStyleFamily;

import java.util.HashMap;
import java.util.Map;

/**
 * [NOTE] Sub-classing UISkin into UISkinWithUrn is a work-around in order to associate a ResourceUrn with a UISkin.
 * When a UISkin is deserialised (via UISkinTypeHandler), then it is copied into a new UISkinWithUrn instance to associate it with the urn it was deserialised with.
 *
 * (2021-03-31) It is not clear whether this feature is used anywhere (in the Omega module space) right now.
 */
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
