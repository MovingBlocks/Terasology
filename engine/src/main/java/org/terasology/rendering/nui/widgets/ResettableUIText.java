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

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;

public class ResettableUIText extends UIText {
    private UIButton clearButton = new UIButton("clearButton", "X");

    @Override
    public void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (super.text.get() == null) {
            super.text.set("");
        }
        super.lastFont = canvas.getCurrentStyle().getFont();
        super.lastWidth = (int) (canvas.size().x * 0.8f);
        if (isEnabled()) {
            canvas.addInteractionRegion(super.interactionListener, Rect2i.createFromMinAndMax(0, 0, (int) (canvas.size().x * 0.8f), canvas.size().y));
            clearButton.subscribe(new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget widget) {
                    setText("");
                }
            });
        }
        clearButton.setEnabled(isEnabled());
        correctCursor();

        int widthForDraw = (super.multiline) ? (int) (canvas.size().x * 0.8f) : super.lastFont.getWidth(getText());

        try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true);
             SubRegion ignored2 = canvas.subRegion(Rect2i.createFromMinAndSize(-super.offset, 0, widthForDraw + 1, Integer.MAX_VALUE), false)) {
            canvas.drawText(super.text.get(), canvas.getRegion());
            if (isFocused()) {
                if (hasSelection()) {
                    drawSelection(canvas);
                } else {
                    drawCursor(canvas);
                }
            }
        }
        Rect2i buttonRegion = Rect2i.createFromMinAndMax((int) (canvas.size().x * 0.8f), 0, canvas.size().x, canvas.size().y);
        canvas.drawWidget(clearButton, buttonRegion);
    }
}
