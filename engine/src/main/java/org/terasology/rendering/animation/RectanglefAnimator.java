// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.animation;

import com.google.common.base.Preconditions;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglefc;

import java.util.function.Consumer;


/**
 * Interpolates rectangles
 */
public class RectanglefAnimator implements Animator {

    private final Rectanglefc from;
    private final Rectanglefc to;
    private Consumer<Rectanglef> consumer;

    /**
     * @param from the left hand value to interpolate between
     * @param to the right hand value to interpolate between
     * @param consumer the target of this animator
     */
    public RectanglefAnimator(Rectanglef from, Rectanglef to, Consumer<Rectanglef> consumer) {
        Preconditions.checkArgument(from != null);
        Preconditions.checkArgument(to != null);
        Preconditions.checkArgument(consumer != null);
        this.from = from;
        this.to = to;
        this.consumer = consumer;
    }

    @Override
    public void apply(float v) {
        consumer.accept(new Rectanglef(
            // rounds towards zero
            (v * (to.minX() - from.minX()) + from.minX()),
            (v * (to.minY() - from.minY()) + from.minY()),
            (v * (to.maxX() - from.maxX()) + from.maxX()),
            (v * (to.maxY() - from.maxY()) + from.maxY())
        ));
    }
}
