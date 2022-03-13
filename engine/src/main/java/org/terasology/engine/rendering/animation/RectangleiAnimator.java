// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

import com.google.common.base.Preconditions;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.joml.geom.Rectangleic;

import java.util.function.Consumer;

/**
 * Interpolates rectangles
 */
public class RectangleiAnimator implements Animator {

    private final Rectangleic from;
    private final Rectangleic to;
    private Consumer<Rectanglei> consumer;

    /**
     * @param from the left hand value to interpolate between
     * @param to the right hand value to interpolate between
     * @param consumer the target of this animator
     */
    public RectangleiAnimator(Rectanglei from, Rectanglei to, Consumer<Rectanglei> consumer) {
        Preconditions.checkArgument(from != null);
        Preconditions.checkArgument(to != null);
        Preconditions.checkArgument(consumer != null);
        this.from = from;
        this.to = to;
        this.consumer = consumer;
    }

    @Override
    public void apply(float v) {
        consumer.accept(new Rectanglei(
            // rounds towards zero
            (int) (v * (to.minX() - from.minX()) + from.minX()),
            (int) (v * (to.minY() - from.minY()) + from.minY()),
            (int) (v * (to.maxX() - from.maxX()) + from.maxX()),
            (int) (v * (to.maxY() - from.maxY()) + from.maxY())
        ));
    }
}
