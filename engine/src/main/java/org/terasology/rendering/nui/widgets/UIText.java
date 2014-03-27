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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.input.Keyboard;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.MouseInput;
import org.terasology.input.events.KeyEvent;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
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

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Immortius
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

    private List<ActivateEventListener> listeners = Lists.newArrayList();

    private int offset;

    private InteractionListener interactionListener = new BaseInteractionListener() {
        boolean dragging;

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                moveCursor(pos, false);
                dragging = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseDrag(Vector2i pos) {
            if (dragging) {
                moveCursor(pos, true);
            }
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                dragging = false;
            }
        }
    };

    public UIText() {
        cursorTexture = Assets.getTexture("engine:white");
    }

    public UIText(String id) {
        super(id);
        cursorTexture = Assets.getTexture("engine:white");
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

        int widthForDraw = (multiline) ?  canvas.size().x : lastFont.getWidth(getText());

        try (SubRegion ignored = canvas.subRegion(canvas.getRegion(), true);
             SubRegion ignored2 = canvas.subRegion(Rect2i.createFromMinAndSize(-offset, 0, widthForDraw, Integer.MAX_VALUE), false)) {
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

        int start = Math.min(cursorPosition, selectionStart);
        int end = Math.max(cursorPosition, selectionStart);

        String beforeCursor = text.get().substring(0, start);
        String selectionText = text.get().substring(start, end);

        Color textColor = canvas.getCurrentStyle().getTextColor();

        // TODO: Support different text alignments
        List<String> linesBefore = TextLineBuilder.getLines(font, beforeCursor, canvas.size().x);
        List<String> linesSelected = TextLineBuilder.getLines(font, selectionText, canvas.size().x);
        int lastLineWidth = font.getWidth(linesBefore.get(linesBefore.size() - 1));

        for (int i = 0; i < linesSelected.size(); i++) {
            int startX = (i == 0) ? lastLineWidth : 0;
            Vector2i selectionTopLeft = new Vector2i(startX, (i + linesBefore.size() - 1) * font.getLineHeight());

            String selLine = linesSelected.get(i);
            int selectionWidth = font.getWidth(selLine);

            Rect2i region = Rect2i.createFromMinAndSize(selectionTopLeft.x, selectionTopLeft.y, selectionWidth, font.getLineHeight());

            canvas.drawTexture(cursorTexture, region, textColor);
            canvas.drawTextRaw(selLine, font, textColor.inverse(), region);
        }
    }

    private void drawCursor(Canvas canvas) {
        if (blinkCounter < BLINK_RATE) {
            Font font = canvas.getCurrentStyle().getFont();
            String beforeCursor = text.get();
            if (cursorPosition < text.get().length()) {
                beforeCursor = beforeCursor.substring(0, cursorPosition);
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
    public void onKeyEvent(KeyEvent event) {
        correctCursor();
        if (event.isDown() && lastFont != null) {
            String fullText = text.get();

            switch (event.getKey().getId()) {
                case KeyId.LEFT: {
                    if (hasSelection() && !isSelectionModifierActive()) {
                        cursorPosition = Math.min(cursorPosition, selectionStart);
                        selectionStart = cursorPosition;
                    } else if (cursorPosition > 0) {
                        cursorPosition--;
                        if (!isSelectionModifierActive()) {
                            selectionStart = cursorPosition;
                        }
                    }
                    event.consume();
                    break;
                }
                case KeyId.RIGHT: {
                    if (hasSelection() && !isSelectionModifierActive()) {
                        cursorPosition = Math.max(cursorPosition, selectionStart);
                        selectionStart = cursorPosition;
                    } else if (cursorPosition < fullText.length()) {
                        cursorPosition++;
                        if (!isSelectionModifierActive()) {
                            selectionStart = cursorPosition;
                        }
                    }
                    event.consume();
                    break;
                }
                case KeyId.HOME: {
                    cursorPosition = 0;
                    offset = 0;
                    if (!isSelectionModifierActive()) {
                        selectionStart = cursorPosition;
                    }
                    event.consume();
                    break;
                }
                case KeyId.END: {
                    cursorPosition = fullText.length();
                    if (!isSelectionModifierActive()) {
                        selectionStart = cursorPosition;
                    }
                    event.consume();
                    break;
                }
                default: {
                    if (Keyboard.isKeyDown(KeyId.LEFT_CTRL) || Keyboard.isKeyDown(KeyId.RIGHT_CTRL)) {
                        if (event.getKey() == Keyboard.Key.C) {
                            copySelection();
                            event.consume();
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
                        } else if (cursorPosition > 0) {
                            String before = fullText.substring(0, cursorPosition - 1);
                            String after = fullText.substring(cursorPosition);
                            setText(before + after);
                            cursorPosition--;
                            selectionStart = cursorPosition;
                        }
                        event.consume();
                        break;
                    }
                    case KeyId.DELETE: {
                        if (hasSelection()) {
                            removeSelection();
                        } else if (cursorPosition < fullText.length()) {
                            String before = fullText.substring(0, cursorPosition);
                            String after = fullText.substring(cursorPosition + 1);
                            setText(before + after);
                        }
                        event.consume();
                        break;
                    }
                    case KeyId.ENTER: {
                        for (ActivateEventListener listener : listeners) {
                            listener.onActivated(this);
                        }
                        event.consume();
                        break;
                    }
                    default: {
                        if (Keyboard.isKeyDown(KeyId.LEFT_CTRL) || Keyboard.isKeyDown(KeyId.RIGHT_CTRL)) {
                            if (event.getKey() == Keyboard.Key.V) {
                                removeSelection();
                                paste();
                                event.consume();
                                break;
                            } else if (event.getKey() == Keyboard.Key.X) {
                                copySelection();
                                removeSelection();
                                event.consume();
                                break;
                            }
                        }
                        if (event.getKeyCharacter() != 0 && lastFont.hasCharacter(event.getKeyCharacter())) {
                            String before = fullText.substring(0, Math.min(cursorPosition, selectionStart));
                            String after = fullText.substring(Math.max(cursorPosition, selectionStart));
                            setText(before + event.getKeyCharacter() + after);
                            cursorPosition = Math.min(cursorPosition, selectionStart) + 1;
                            selectionStart = cursorPosition;
                            event.consume();
                        }
                        break;
                    }
                }
            }
        }
        correctCursor();
        updateOffset();
    }

    private void updateOffset() {
        if (lastFont != null && !multiline) {
            String before = getText().substring(0, cursorPosition);
            int cursorDist = lastFont.getWidth(before);
            if (cursorDist < offset) {
                offset = cursorDist;
            }
            if (cursorDist > offset + lastWidth) {
                offset = cursorDist - lastWidth + 1;
            }
        }
    }

    private boolean isSelectionModifierActive() {
        return Keyboard.isKeyDown(KeyId.LEFT_SHIFT) || Keyboard.isKeyDown(KeyId.RIGHT_SHIFT);
    }

    private boolean hasSelection() {
        return cursorPosition != selectionStart;
    }

    private void removeSelection() {
        if (hasSelection()) {
            String before = getText().substring(0, Math.min(cursorPosition, selectionStart));
            String after = getText().substring(Math.max(cursorPosition, selectionStart));
            setText(before + after);
            cursorPosition = Math.min(cursorPosition, selectionStart);
            selectionStart = cursorPosition;
        }
    }

    private void copySelection() {
        if (hasSelection()) {
            String fullText = getText();
            String selection = fullText.substring(Math.min(selectionStart, cursorPosition), Math.max(selectionStart, cursorPosition));
            setClipboardContents(selection);
        }
    }

    private void paste() {
        String fullText = getText();
        String before = fullText.substring(0, cursorPosition);
        String after = fullText.substring(cursorPosition);
        String pasted = getClipboardContents();
        setText(before + pasted + after);
        cursorPosition += pasted.length();
        selectionStart = cursorPosition;
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

    private void moveCursor(Vector2i pos, boolean selecting) {
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
            cursorPosition = Math.min(newCursorPos, rawText.length());
            if (!isSelectionModifierActive() && !selecting) {
                selectionStart = cursorPosition;
            }

            updateOffset();
        }
    }

    public void setCursorPosition(int position) {
        this.cursorPosition = TeraMath.clamp(position, 0, getText().length());
        this.selectionStart = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void bindText(Binding<String> binding) {
        text = binding;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String val) {
        text.set(val);
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
        listeners.add(listener);
    }

    public void unsubscribe(ActivateEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        blinkCounter += delta;
        while (blinkCounter > 2 * BLINK_RATE) {
            blinkCounter -= 2 * BLINK_RATE;
        }
    }

    private void correctCursor() {
        cursorPosition = TeraMath.clamp(cursorPosition, 0, getText().length());
        selectionStart = TeraMath.clamp(selectionStart, 0, getText().length());
    }
}
