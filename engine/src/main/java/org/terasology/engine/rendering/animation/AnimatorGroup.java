// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.animation;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Groups animators
 */
public class AnimatorGroup implements Animator {

    private final List<Animator> animators;

    /**
     * Groups a list of animators and runs them in the same order as in the list
     * @param animators a list of animators (will be copied)
     */
    public AnimatorGroup(Animator... animators) {
        Preconditions.checkArgument(animators.length > 0);
        this.animators = new ArrayList<>(Arrays.asList(animators));
    }

    @Override
    public void apply(float value) {
        for (Animator anim : animators) {
            anim.apply(value);
        }
    }

}
