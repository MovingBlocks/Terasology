// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

/*
 * Interface for interpolation equations.
 */
public interface Animator {
    /**
     * Returns where an interpolated value should be based on
     * where the position an animation is in.
     *
     * @param value position of the animation between the start and end [0:1]
     * and also referenced as the intermediate interpolation <b>value</b>.
     */
    void apply(float value);
}
