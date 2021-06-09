// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.terasology.engine.network.Replicate;

public final class LightFadeComponent implements VisualComponent<LightFadeComponent> {

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

    @Override
    public void copy(LightFadeComponent other) {
        this.targetDiffuseIntensity = other.targetDiffuseIntensity;
        this.targetAmbientIntensity = other.targetAmbientIntensity;
        this.removeLightAfterFadeComplete = other.removeLightAfterFadeComplete;
        this.diffuseFadeRate = other.diffuseFadeRate;
        this.ambientFadeRate = other.ambientFadeRate;
    }
}
