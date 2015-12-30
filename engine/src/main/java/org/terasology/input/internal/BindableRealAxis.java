/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.input.internal;

import org.terasology.input.BindAxisEvent;

/**
 * This implementation is linked to a real axis.
 */
public class BindableRealAxis extends AbstractBindableAxis {

    private float targetValue;

    public BindableRealAxis(String id, BindAxisEvent event) {
        super(id, event);
    }

    @Override
    protected float getTargetValue() {
        return targetValue;
    }

    /**
     * @param targetValue
     */
    public void setTargetValue(float targetValue) {
        this.targetValue = targetValue;
    }
}
