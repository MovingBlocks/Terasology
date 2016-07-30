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

import com.google.common.base.Preconditions;
import org.terasology.math.geom.Rect2f;

import java.util.function.Consumer;


/**
 * Interpolates rectangles
 */
public class Rect2fAnimator implements Animator {

    private final Rect2f from;
    private final Rect2f to;
    private Consumer<Rect2f> consumer;

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
