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

package org.terasology.rendering.nui.databinding;

import com.google.common.math.DoubleMath;

import java.math.RoundingMode;

/**
 * Links a Integer-Binding to a Float-Binding
 */
public final class IntToFloatBinding implements Binding<Float> {

    private final Binding<Integer> intBinding;
    private final RoundingMode roundingMode;

    /**
     * Uses {@link RoundingMode#HALF_UP}.
     * @param intBinding the original binding that is wrapped
     */
    public IntToFloatBinding(Binding<Integer> intBinding) {
        this(intBinding, RoundingMode.HALF_UP);
    }

    public IntToFloatBinding(Binding<Integer> intBinding, RoundingMode rm) {
        this.intBinding = intBinding;
        this.roundingMode = rm;
    }

    @Override
    public Float get() {
        return Float.valueOf(intBinding.get());
    }

    @Override
    public void set(Float value) {
        int val = DoubleMath.roundToInt(value, roundingMode);
        intBinding.set(val);
    }
}

