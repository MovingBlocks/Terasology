/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.world.generator.params;

import org.terasology.rendering.nui.databinding.Binding;

/**
 * Represents an integer parameter
 * @author Martin Steiger
 */
public class IntParameter extends AbstractParameter<Integer> {
    private int max;
    private int min;

    /**
     * @param label the human-readable label
     * @param binding the value binding
     * @param min the min value
     * @param max the max value
     */
    public IntParameter(String label, Binding<Integer> binding, int min, int max) {
        super(label, binding);
        
        this.min = min;
        this.max = max;
    }

    /**
     * @return the max value
     */
    public int getMax() {
        return max;
    }

    /**
     * @return the min value
     */
    public int getMin() {
        return min;
    }

}
