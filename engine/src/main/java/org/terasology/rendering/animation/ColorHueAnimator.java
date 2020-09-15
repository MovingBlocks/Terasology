/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.animation;

import java.util.List;
import java.util.function.Consumer;

import org.terasology.nui.Color;
import org.terasology.rendering.nui.layers.mainMenu.settings.CieCamColors;

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
