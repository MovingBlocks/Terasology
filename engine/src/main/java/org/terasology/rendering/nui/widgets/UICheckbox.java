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
package org.terasology.rendering.nui.widgets;

import org.terasology.input.MouseInput;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;

/**
 * A check-box. Hovering is supported.
 */
public class UICheckbox extends CoreWidget {
    public static final String HOVER_ACTIVE_MODE = "hover-active";

    private Binding<Boolean> active = new DefaultBinding<>(false);

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
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
        if (isEnabled()) {
            canvas.addInteractionRegion(interactionListener);
        }
    }

    @Override
    public String getMode() {
        if (!isEnabled()) {
            return DISABLED_MODE;
        } else if (interactionListener.isMouseOver()) {
            if (active.get()) {
                return HOVER_ACTIVE_MODE;
            }
            return HOVER_MODE;
        } else if (active.get()) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }

    /**
     * @return A boolean indicating the status of the checkbox
     */
    public boolean isChecked() {
        return active.get();
    }

    /**
     * @param checked A boolean setting the ticked state of the checkbox
     */
    public void setChecked(boolean checked) {
        active.set(checked);
    }


    public void bindChecked(Binding<Boolean> binding) {
        this.active = binding;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return Vector2i.zero();
    }
}
