// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

import org.terasology.nui.Color;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.CieCamColors;

import java.util.List;
import java.util.function.Consumer;

/**
 * Interpolates color hue based on the CIECam02 color model.
 */
public class ColorHueAnimator implements Animator {

    private Consumer<Color> consumer;
    private final List<Color> colors = CieCamColors.L65C65;

    /**
     * @param consumer the target of this animator
     */
    public ColorHueAnimator(Consumer<Color> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void apply(float v) {
        int count = colors.size();
        int index = (v == 1.0) ? count - 1 : (int) (v * count);
        Color color = colors.get(index);
        consumer.accept(color);
    }
}
