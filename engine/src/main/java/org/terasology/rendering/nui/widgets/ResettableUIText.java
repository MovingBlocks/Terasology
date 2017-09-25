/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import org.terasology.input.MouseInput;
import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;

/**
 * A text widget with a button to clear the text.
 */
public class ResettableUIText extends UIText {

    private InteractionListener clearInteractionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                setText("");
                return true;
            }
            return false;
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        Rect2i clearButtonRegion = Rect2i.createFromMinAndSize(0, 0, 30, canvas.size().y);
        lastWidth = canvas.size().x - clearButtonRegion.size().x;
        if (isEnabled()) {
            canvas.addInteractionRegion(interactionListener, Rect2i.createFromMinAndMax(0, 0, canvas.size().x, canvas.size().y));
            canvas.addInteractionRegion(clearInteractionListener, Rect2i.createFromMinAndMax(canvas.size().x, 0, canvas.size().x +
                    clearButtonRegion.size().x, canvas.size().y));
        }
        drawAll(canvas, canvas.size().x - clearButtonRegion.size().x);
    }
}
