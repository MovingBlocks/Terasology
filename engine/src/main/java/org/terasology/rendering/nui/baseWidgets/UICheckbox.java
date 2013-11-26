/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.baseWidgets;

import org.terasology.input.MouseInput;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Immortius
 */
public class UICheckbox extends AbstractWidget {
    public static final String HOVER_ACTIVE_MODE = "hover-active";

    private Binding<Boolean> active = new DefaultBinding<>(false);

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                active.set(!active.get());
                return true;
            }
            return false;
        }

    };

    public UICheckbox() {
    }

    public UICheckbox(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(interactionListener);
    }

    @Override
    public String getMode() {
        if (interactionListener.isMouseOver()) {
            if (active.get()) {
                return HOVER_ACTIVE_MODE;
            }
            return HOVER_MODE;
        } else if (active.get()) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }

    public boolean isChecked() {
        return active.get();
    }

    public void setChecked(boolean checked) {
        active.set(checked);
    }

    public void bindChecked(Binding<Boolean> binding) {
        this.active = binding;
    }

}
