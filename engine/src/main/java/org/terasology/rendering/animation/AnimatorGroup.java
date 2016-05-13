/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

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
