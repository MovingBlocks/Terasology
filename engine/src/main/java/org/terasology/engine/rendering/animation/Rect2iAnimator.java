// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

import com.google.common.base.Preconditions;
import org.terasology.math.geom.Rect2i;

import java.util.function.Consumer;

/**
 * Interpolates rectangles
 */
public class Rect2iAnimator implements Animator {

    private final Rect2i from;
    private final Rect2i to;
    private final Consumer<Rect2i> consumer;

    /**
     * @param from the left hand value to interpolate between
     * @param to the right hand value to interpolate between
     * @param consumer the target of this animator
     */
    public Rect2iAnimator(Rect2i from, Rect2i to, Consumer<Rect2i> consumer) {
        Preconditions.checkArgument(from != null);
        Preconditions.checkArgument(to != null);
        Preconditions.checkArgument(consumer != null);
        this.from = from;
        this.to = to;
        this.consumer = consumer;
    }

    @Override
    public void apply(float v) {
        consumer.accept(Rect2i.createFromMinAndMax(
            // rounds towards zero
            (int) (v * (to.minX() - from.minX()) + from.minX()),
            (int) (v * (to.minY() - from.minY()) + from.minY()),
            (int) (v * (to.maxX() - from.maxX()) + from.maxX()),
            (int) (v * (to.maxY() - from.maxY()) + from.maxY())
        ));
    }
}
