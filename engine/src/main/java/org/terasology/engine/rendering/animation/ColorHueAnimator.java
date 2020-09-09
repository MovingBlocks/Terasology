// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

import java.util.List;
import java.util.function.Consumer;

import org.terasology.nui.Color;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.CieCamColors;

/**
 * Interpolates color hue based on the CIECam02 color model.
 */
public class ColorHueAnimator implements Animator {

    private final Consumer<Color> consumer;
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
