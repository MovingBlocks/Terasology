// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

import com.google.common.base.Preconditions;
import org.terasology.math.geom.Rect2f;

import java.util.function.Consumer;


/**
 * Interpolates rectangles
 */
public class Rect2fAnimator implements Animator {

    private final Rect2f from;
    private final Rect2f to;
    private final Consumer<Rect2f> consumer;

    /**
     * @param from the left hand value to interpolate between
     * @param to the right hand value to interpolate between
     * @param consumer the target of this animator
     */
    public Rect2fAnimator(Rect2f from, Rect2f to, Consumer<Rect2f> consumer) {
        Preconditions.checkArgument(from != null);
        Preconditions.checkArgument(to != null);
        Preconditions.checkArgument(consumer != null);
        this.from = from;
        this.to = to;
        this.consumer = consumer;
    }

    @Override
    public void apply(float v) {
        consumer.accept(Rect2f.createFromMinAndMax(
            // rounds towards zero
            (v * (to.minX() - from.minX()) + from.minX()),
            (v * (to.minY() - from.minY()) + from.minY()),
            (v * (to.maxX() - from.maxX()) + from.maxX()),
            (v * (to.maxY() - from.maxY()) + from.maxY())
        ));
    }
}
