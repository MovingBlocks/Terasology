/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.input.Keyboard;
import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.events.NUIKeyEvent;

/**
 * A text widget with a hint string placeholder.
 */
public class UITextHint extends UIText {
    /* The placeholder hint text. */
    private boolean isShowingHintText = true;
    @LayoutConfig
    private String hintText = "";

    @Override
    public void onDraw(Canvas canvas) {
        if (text.get() == null) {
            text.set("");
        }
        if (text.get().equals("")) {
            text.set(hintText);
            isShowingHintText = true;
        }
        if (isShowingHintText) {
            setCursorPosition(0);
            if (!text.get().equals(hintText) && text.get().endsWith(hintText)) {
                text.set(text.get().substring(0, text.get().length()-hintText.length()));
                setCursorPosition(text.get().length());
                isShowingHintText = false;
            }
        }
        lastFont = canvas.getCurrentStyle().getFont();
        lastWidth = canvas.size().x;
        if (isEnabled()) {
            canvas.addInteractionRegion(interactionListener, canvas.getRegion());
        }
        correctCursor();

        int widthForDraw = (multiline) ? canvas.size().x : lastFont.getWidth(getText());

        try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true);
             SubRegion ignored2 = canvas.subRegion(Rect2i.createFromMinAndSize(-offset, 0, widthForDraw + 1, Integer.MAX_VALUE), false)) {
            if (isShowingHintText && !readOnly) {
                canvas.drawTextRaw(text.get(), lastFont, canvas.getCurrentStyle().getHintTextColor(), canvas.getRegion());
            } else {
                canvas.drawText(text.get(), canvas.getRegion());
            }
            if (isFocused()) {
                if (hasSelection()) {
                    drawSelection(canvas);
                } else {
                    drawCursor(canvas);
                }
            }
        }
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        boolean eventHandled = false;
        if (isShowingHintText && !readOnly) {
            if (event.getKeyboard().isKeyDown(Keyboard.KeyId.LEFT_CTRL)
                    || event.getKeyboard().isKeyDown(Keyboard.KeyId.RIGHT_CTRL)) {
                if (event.getKey() == Keyboard.Key.V) {
                    removeSelection();
                    paste();
                    eventHandled = true;
                }
            }
            if (event.getKeyCharacter() != 0 && lastFont.hasCharacter(event.getKeyCharacter())) {
                String fullText = text.get();
                String before = fullText.substring(0, Math.min(getCursorPosition(), selectionStart));
                String after = fullText.substring(Math.max(getCursorPosition(), selectionStart));
                setText(before + event.getKeyCharacter() + after);
                setCursorPosition(Math.min(getCursorPosition(), selectionStart) + 1);
            }
        } else {
            eventHandled = super.onKeyEvent(event);
        }
        return eventHandled;
    }

    @Override
    protected void drawSelection(Canvas canvas) {
        super.drawSelection(canvas);
    }
}
