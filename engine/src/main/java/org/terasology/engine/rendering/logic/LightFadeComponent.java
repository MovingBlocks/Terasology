// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.terasology.engine.network.Replicate;

public final class LightFadeComponent implements VisualComponent {

    @Replicate
    public float targetDiffuseIntensity = 1.0f;

    @Replicate
    public float targetAmbientIntensity = 1.0f;

    @Replicate
    public boolean removeLightAfterFadeComplete;

    @Replicate
    public float diffuseFadeRate = 2.0f;

    @Replicate
    public float ambientFadeRate = 2.0f;

}
