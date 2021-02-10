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
import org.terasology.input.BindableButton;
import org.terasology.input.ButtonState;

/**
 * This implementation is linked to a positive BindableButton (that pushes the axis towards 1)
 * and a negative BindableButton (that pushes it towards -1).
 */
public class BindableAxisImpl extends AbstractBindableAxis {
    private BindableButton positiveInput;
    private BindableButton negativeInput;

    public BindableAxisImpl(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        super(id, event);
        this.positiveInput = positiveButton;
        this.negativeInput = negativeButton;
    }

    @Override
    protected float getTargetValue() {
        boolean posInput = positiveInput.getState() == ButtonState.DOWN;
        boolean negInput = negativeInput.getState() == ButtonState.DOWN;

        float targetValue = 0;
        if (posInput) {
            targetValue += 1.0f;
        }
        if (negInput) {
            targetValue -= 1.0f;
        }

        return targetValue;
    }
}
