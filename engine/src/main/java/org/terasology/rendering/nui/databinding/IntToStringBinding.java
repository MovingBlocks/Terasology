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

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

/**
 * Links a Integer-{@link Binding} to a String-{@link Binding}.
 * Also allows to restrict the valid value to a {@link Range}.
 */
public final class IntToStringBinding implements Binding<String> {

    private final Binding<Integer> intBinding;
    private final Range<Integer> validRange;
    
    /**
     * @param intBinding the original binding
     */
    public IntToStringBinding(Binding<Integer> intBinding) {
        this(intBinding, Range.<Integer>all());
    }
    
    public IntToStringBinding(Binding<Integer> intBinding, Range<Integer> validRange) {
        Preconditions.checkNotNull(intBinding, "the binding must not be null");
        Preconditions.checkNotNull(validRange, "the valid range must not be null");
        
        this.intBinding = intBinding;
        this.validRange = validRange;
    }

    /**
     * @return the value or empty string if the original binding returns <code>null</code>
     */
    @Override
    public String get() {
        Integer i = intBinding.get();
        return i == null ? "" : i.toString();
    }

    @Override
    public void set(String text) {
        Integer val = Ints.tryParse(text);
        if (val != null && validRange.contains(val)) {
            // An alternative would be to clamp the value to min/max
            intBinding.set(val);
        }
    }
}

