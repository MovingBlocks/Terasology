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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.input.Keyboard;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.FontUnderline;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public class UIText extends CoreWidget {

    private static final Logger logger = LoggerFactory.getLogger(UIText.class);

    private static final float BLINK_RATE = 0.25f;

    private float blinkCounter;

    private TextureRegion cursorTexture;
    private Binding<String> text = new DefaultBinding<>("");

    @LayoutConfig
    private boolean multiline;

    @LayoutConfig
    private boolean readOnly;

    private int cursorPosition;
    private int selectionStart;

    private int lastWidth;
    private Font lastFont;

    private List<ActivateEventListener> activationListeners = Lists.newArrayList();
    private List<CursorUpdateEventListener> cursorUpdateListeners = Lists.newArrayList();
    private List<TextChangeEventListener> textChangeListeners = Lists.newArrayList();

    private int offset;

    private InteractionListener interactionListener = new BaseInteractionListener() {
        boolean dragging;

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                moveCursor(event.getRelativeMousePosition(), false, event.getKeyboard());
                dragging = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            if (dragging) {
                moveCursor(event.getRelativeMousePosition(), true, event.getKeyboard());
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                dragging = false;
            }
        }
    };

    public UIText() {
        cursorTexture = Assets.getTexture("engine:white").get();
    }

    public UIText(String id) {
        super(id);
        cursorTexture = Assets.getTexture("engine:white").get();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (text.get() == null) {
            text.set("");
        }
        lastFont = canvas.getCurrentStyle().getFont();
        lastWidth = canvas.size().x;
        canvas.addInteractionRegion(interactionListener, canvas.getRegion());
        correctCursor();

        int widthForDraw = (multiline) ? canvas.size().x : lastFont.getWidth(getText());

        try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true);
             SubRegion ignored2 = canvas.subRegion(Rect2i.createFromMinAndSize(-offset, 0, widthForDraw + 1, Integer.MAX_VALUE), false)) {
            canvas.drawText(text.get(), canvas.getRegion());
            if (isFocused()) {
                if (hasSelection()) {
                    drawSelection(canvas);
                } else {
                    drawCursor(canvas);
                }
            }
        }
    }

    private void drawSelection(Canvas canvas) {
        Font font = canvas.getCurrentStyle().getFont();
        String currentText = getText();

        int start = Math.min(getCursorPosition(), selectionStart);
        int end = Math.max(getCursorPosition(), selectionStart);

        Color textColor = canvas.getCurrentStyle().getTextColor();
        int canvasWidth = (multiline) ? canvas.size().x : Integer.MAX_VALUE;

        // TODO: Support different text alignments
        List<String> rawLinesAfterCursor = TextLineBuilder.getLines(font, currentText, Integer.MAX_VALUE);
        int currentChar = 0;
        int lineOffset = 0;
        for (int lineIndex = 0; lineIndex < rawLinesAfterCursor.size() && currentChar <= end; ++lineIndex) {
            String line = rawLinesAfterCursor.get(lineIndex);
            List<String> innerLines = TextLineBuilder.getLines(font, line, canvasWidth);

            for (int innerLineIndex = 0; innerLineIndex < innerLines.size() && currentChar <= end; ++innerLineIndex) {
                String innerLine = innerLines.get(innerLineIndex);
                String selectionString;
                int offsetX = 0;
                if (currentChar + innerLine.length() < start) {
                    selectionString = "";
                } else if (currentChar < start) {
                    offsetX = font.getWidth(innerLine.substring(0, start - currentChar));
                    selectionString = innerLine.substring(start - currentChar, Math.min(end - currentChar, innerLine.length()));
                } else if (currentChar + innerLine.length() >= end) {
                    selectionString = innerLine.substring(0, end - currentChar);
                } else {
                    selectionString = innerLine;
                }
                if (!selectionString.isEmpty()) {
                    int selectionWidth = font.getWidth(selectionString);
                    Vector2i selectionTopLeft = new Vector2i(offsetX, (lineOffset) * font.getLineHeight());
                    Rect2i region = Rect2i.createFromMinAndSize(selectionTopLeft.x, selectionTopLeft.y, selectionWidth, font.getLineHeight());

                    canvas.drawTexture(cursorTexture, region, textColor);
                    canvas.drawTextRaw(FontUnderline.strip(FontColor.stripColor(selectionString)), font, textColor.inverse(), region);
                }
                currentChar += innerLine.length();
                lineOffset++;
            }
            currentChar++;
        }
    }

    private void drawCursor(Canvas canvas) {
        if (blinkCounter < BLINK_RATE) {
            Font font = canvas.getCurrentStyle().getFont();
            String beforeCursor = text.get();
            if (getCursorPosition() < text.get().length()) {
                beforeCursor = beforeCursor.substring(0, getCursorPosition());
            }
            List<String> lines = TextLineBuilder.getLines(font, beforeCursor, canvas.size().x);

            // TODO: Support different alignments

            int lastLineWidth = font.getWidth(lines.get(lines.size() - 1));
            Rect2i region = Rect2i.createFromMinAndSize(lastLineWidth, (lines.size() - 1) * font.getLineHeight(),
                    1, font.getLineHeight());
            canvas.drawTexture(cursorTexture, region, canvas.getCurrentStyle().getTextColor());
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        if (isMultiline()) {
            List<String> lines = TextLineBuilder.getLines(font, text.get(), areaHint.x);
            return font.getSize(lines);
        } else {
            return new Vector2i(font.getWidth(getText()), font.getLineHeight());
        }
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        Font font = canvas.getCurrentStyle().getFont();
        if (isMultiline()) {
            return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            return new Vector2i(Integer.MAX_VALUE, font.getLineHeight());
        }
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        correctCursor();
        boolean eventHandled = false;
        if (event.isDown() && lastFont != null) {
            String fullText = text.get();

            switch (event.getKey().getId()) {
                case KeyId.LEFT: {
                    if (hasSelection() && !isSelectionModifierActive(event.getKeyboard())) {
                        setCursorPosition(Math.min(getCursorPosition(), selectionStart));
                    } else if (getCursorPosition() > 0) {
                        decreaseCursorPosition(1, !isSelectionModifierActive(event.getKeyboard()));
                    }
                    eventHandled = true;
                    break;
                }
                case KeyId.RIGHT: {
                    if (hasSelection() && !isSelectionModifierActive(event.getKeyboard())) {
                        setCursorPosition(Math.max(getCursorPosition(), selectionStart));
                    } else if (getCursorPosition() < fullText.length()) {
                        increaseCursorPosition(1, !isSelectionModifierActive(event.getKeyboard()));
                    }
                    eventHandled = true;
                    break;
                }
                case KeyId.HOME: {
                    setCursorPosition(0, !isSelectionModifierActive(event.getKeyboard()));
                    offset = 0;
                    eventHandled = true;
                    break;
                }
                case KeyId.END: {
                    setCursorPosition(fullText.length(), !isSelectionModifierActive(event.getKeyboard()));
                    eventHandled = true;
                    break;
                }
                default: {
                    if (event.getKeyboard().isKeyDown(KeyId.LEFT_CTRL)
                            || event.getKeyboard().isKeyDown(KeyId.RIGHT_CTRL)) {
                        if (event.getKey() == Keyboard.Key.C) {
                            copySelection();
                            eventHandled = true;
                            break;
                        }
                    }
                }
            }

            if (!readOnly) {
                switch (event.getKey().getId()) {
                    case KeyId.BACKSPACE: {
                        if (hasSelection()) {
                            removeSelection();
                        } else if (getCursorPosition() > 0) {
                            String before = fullText.substring(0, getCursorPosition() - 1);
                            String after = fullText.substring(getCursorPosition());

                            if (getCursorPosition() < fullText.length()) {
                                decreaseCursorPosition(1);
                            }

                            setText(before + after);
                        }
                        eventHandled = true;
                        break;
                    }
                    case KeyId.DELETE: {
                        if (hasSelection()) {
                            removeSelection();
                        } else if (getCursorPosition() < fullText.length()) {
                            String before = fullText.substring(0, getCursorPosition());
                            String after = fullText.substring(getCursorPosition() + 1);
                            setText(before + after);
                        }
                        eventHandled = true;
                        break;
                    }
                    case KeyId.ENTER:
                    case KeyId.NUMPAD_ENTER: {
                        for (ActivateEventListener listener : activationListeners) {
                            listener.onActivated(this);
                        }
                        eventHandled = true;
                        break;
                    }
                    default: {
                        if (event.getKeyboard().isKeyDown(KeyId.LEFT_CTRL)
                                || event.getKeyboard().isKeyDown(KeyId.RIGHT_CTRL)) {
                            if (event.getKey() == Keyboard.Key.V) {
                                removeSelection();
                                paste();
                                eventHandled = true;
                                break;
                            } else if (event.getKey() == Keyboard.Key.X) {
                                copySelection();
                                removeSelection();
                                eventHandled = true;
                                break;
                            }
                        }
                        if (event.getKeyCharacter() != 0 && lastFont.hasCharacter(event.getKeyCharacter())) {
                            String before = fullText.substring(0, Math.min(getCursorPosition(), selectionStart));
                            String after = fullText.substring(Math.max(getCursorPosition(), selectionStart));
                            setText(before + event.getKeyCharacter() + after);
                            setCursorPosition(Math.min(getCursorPosition(), selectionStart) + 1);
                            eventHandled = true;
                        }
                        break;
                    }
                }
            }
        }
        updateOffset();
        return eventHandled;
    }

    private void updateOffset() {
        if (lastFont != null && !multiline) {
            String before = getText().substring(0, getCursorPosition());
            int cursorDist = lastFont.getWidth(before);
            if (cursorDist < offset) {
                offset = cursorDist;
            }
            if (cursorDist > offset + lastWidth) {
                offset = cursorDist - lastWidth + 1;
            }
        }
    }

    private boolean isSelectionModifierActive(KeyboardDevice keyboard) {
        return keyboard.isKeyDown(KeyId.LEFT_SHIFT) || keyboard.isKeyDown(KeyId.RIGHT_SHIFT);
    }

    private boolean hasSelection() {
        return getCursorPosition() != selectionStart;
    }

    private void removeSelection() {
        if (hasSelection()) {
            String before = getText().substring(0, Math.min(getCursorPosition(), selectionStart));
            String after = getText().substring(Math.max(getCursorPosition(), selectionStart));
            setText(before + after);
            setCursorPosition(Math.min(getCursorPosition(), selectionStart));
        }
    }

    private void copySelection() {
        if (hasSelection()) {
            String fullText = getText();
            String selection = fullText.substring(Math.min(selectionStart, getCursorPosition()), Math.max(selectionStart, getCursorPosition()));
            setClipboardContents(FontUnderline.strip(FontColor.stripColor(selection)));
        }
    }

    private void paste() {
        String fullText = getText();
        String before = fullText.substring(0, getCursorPosition());
        String after = fullText.substring(getCursorPosition());
        String pasted = getClipboardContents();
        setText(before + pasted + after);
        increaseCursorPosition(pasted.length());
    }

    private String getClipboardContents() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) t.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            logger.warn("Failed to get data from clipboard", e);
        }

        return "";
    }

    private void setClipboardContents(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    }

    private void moveCursor(Vector2i pos, boolean selecting, KeyboardDevice keyboard) {
        if (lastFont != null) {
            pos.x += offset;
            String rawText = getText();
            List<String> lines = TextLineBuilder.getLines(lastFont, rawText, Integer.MAX_VALUE);
            int targetLineIndex = pos.y / lastFont.getLineHeight();
            int passedLines = 0;
            int newCursorPos = 0;
            for (int lineIndex = 0; lineIndex < lines.size() && passedLines <= targetLineIndex; lineIndex++) {
                List<String> subLines;
                if (multiline) {
                    subLines = TextLineBuilder.getLines(lastFont, lines.get(lineIndex), lastWidth);
                } else {
                    subLines = Arrays.asList(lines.get(lineIndex));
                }
                if (subLines.size() + passedLines > targetLineIndex) {
                    for (String subLine : subLines) {
                        if (passedLines == targetLineIndex) {
                            int totalWidth = 0;
                            for (char c : subLine.toCharArray()) {
                                int charWidth = lastFont.getWidth(c);
                                if (totalWidth + charWidth / 2 >= pos.x) {
                                    break;
                                }
                                newCursorPos++;
                                totalWidth += charWidth;
                            }
                            passedLines++;
                            break;
                        } else {
                            newCursorPos += subLine.length();
                            passedLines++;
                        }
                    }
                } else {
                    passedLines += subLines.size();
                    newCursorPos += lines.get(lineIndex).length() + 1;
                }
            }

            setCursorPosition(Math.min(newCursorPos, rawText.length()), !isSelectionModifierActive(keyboard) && !selecting);
            updateOffset();
        }
    }

    public void bindText(Binding<String> binding) {
        text = binding;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String val) {
        String prevText = getText();
        boolean callEvent = !prevText.equals(val);

        text.set(val != null ? val : "");
        correctCursor();

        if (callEvent) {
            for (TextChangeEventListener listener : textChangeListeners) {
                    listener.onTextChange(prevText, val);
                }
        }
    }

    public boolean isMultiline() {
        return multiline;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public void subscribe(ActivateEventListener listener) {
        Preconditions.checkNotNull(listener);
        activationListeners.add(listener);
    }

    public void unsubscribe(ActivateEventListener listener) {
        Preconditions.checkNotNull(listener);
        activationListeners.remove(listener);
    }

    public void subscribe(CursorUpdateEventListener listener) {
        Preconditions.checkNotNull(listener);
        cursorUpdateListeners.add(listener);
    }

    public void unsubscribe(CursorUpdateEventListener listener) {
        Preconditions.checkNotNull(listener);
        cursorUpdateListeners.remove(listener);
    }

    public void subscribe(TextChangeEventListener listener) {
        Preconditions.checkNotNull(listener);
        textChangeListeners.add(listener);
    }

    public void unsubscribe(TextChangeEventListener listener) {
        Preconditions.checkNotNull(listener);
        textChangeListeners.remove(listener);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        blinkCounter += delta;
        while (blinkCounter > 2 * BLINK_RATE) {
            blinkCounter -= 2 * BLINK_RATE;
        }
    }

    public int increaseCursorPosition(int delta, boolean moveSelectionStart) {
        int newPosition = getCursorPosition() + delta;

        setCursorPosition(newPosition, moveSelectionStart);

        return newPosition;
    }

    public int increaseCursorPosition(int delta) {
        return increaseCursorPosition(delta, true);
    }

    public int decreaseCursorPosition(int delta, boolean moveSelectionStart) {
        return increaseCursorPosition(-delta, moveSelectionStart);
    }

    public int decreaseCursorPosition(int delta) {
        return decreaseCursorPosition(delta, true);
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int position, boolean moveSelectionStart, boolean callEvent) {
        int previousPosition = cursorPosition;
        cursorPosition = position;

        if (moveSelectionStart) {
            selectionStart = position;
        }

        correctCursor();

        if (callEvent) {
            for (CursorUpdateEventListener listener : cursorUpdateListeners) {
                listener.onCursorUpdated(previousPosition, cursorPosition);
            }
        }
    }

    public void setCursorPosition(int position, boolean moveSelectionStart) {
        setCursorPosition(position, moveSelectionStart, true);
    }

    public void setCursorPosition(int position) {
        setCursorPosition(position, true, true);
    }

    private void correctCursor() {
        cursorPosition = TeraMath.clamp(cursorPosition, 0, getText().length());
        selectionStart = TeraMath.clamp(selectionStart, 0, getText().length());
    }
}
