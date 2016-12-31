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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Keyboard;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
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
import org.terasology.utilities.Assets;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This class describes a generic text-box widget.
 */
public class UIText extends CoreWidget {

    private static final Logger logger = LoggerFactory.getLogger(UIText.class);

    private static final float BLINK_RATE = 0.25f;
    /** The text contained by the text box. */
    @LayoutConfig
    protected Binding<String> text = new DefaultBinding<>("");

    /** Whether the content needs to be displayed on multiple lines. */
    @LayoutConfig
    protected boolean multiline;

    /** Whether the text box is read-only. */
    @LayoutConfig
    protected boolean readOnly;

    /** The position of the cursor in the text box. */
    protected int cursorPosition;

    /** The index in the text where the selection starts. */
    protected int selectionStart;

    /** The last assigned width of the text box. */
    protected int lastWidth;

    /** The font in which text was drawn the last time before the current update. */
    protected Font lastFont;

    /** A list of all activation event listeners (handle what to do when the text box is activated) of the text box. */
    protected List<ActivateEventListener> activationListeners = Lists.newArrayList();

    /** A list of all cursor update event listeners (handle what to do when the cursor is moved) of the text box. */
    protected List<CursorUpdateEventListener> cursorUpdateListeners = Lists.newArrayList();

    /** A list of text change event listeners (handle what to do when the text in the widget is changed) of the text box. */
    protected List<TextChangeEventListener> textChangeListeners = Lists.newArrayList();

    /** The number of characters between the start of the text in the widget and the current position of the cursor. */
    protected int offset;

    /**
     * The interaction listener of the widget. This handles how the widget reacts to different stimuli from the user.
     */
    protected InteractionListener interactionListener = new BaseInteractionListener() {
        boolean dragging;

        /**
         * Defines what to do when the user clicks a mouse button while pointing at the widget. More specifically, it
         * moves the cursor and sets "dragging" to true.
         *
         * @param event The event corresponding to the mouse click
         * @return      Whether a left mouse click was successfully detected and handled.
         */
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                moveCursor(event.getRelativeMousePosition(), false, event.getKeyboard());
                dragging = true;
                return true;
            }
            return false;
        }

        /**
         * Defines what to do when the user drags the mouse in the widget. Specifically, it moves the cursor if the
         * "dragging" variable is set to true (that is, the user is dragging with the left mouse button pressed)
         *
         * @param event The event corresponding to the mouse drag.
         */
        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            if (dragging) {
                moveCursor(event.getRelativeMousePosition(), true, event.getKeyboard());
            }
        }

        /**
         * Defines what to do when a mouse button is released. Specifically, it sets "dragging" to false if the button
         * that was released is the left mouse button.
         *
         * @param event The event corresponding to the releasing of the mouse button.
         */
        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                dragging = false;
            }
        }
    };

    private float blinkCounter;

    private TextureRegion cursorTexture;

    /**
     * Default constructor.
     */
    public UIText() {
        cursorTexture = Assets.getTexture("engine:white").get();
    }

    /**
     * Parametrized constructor.
     *
     * @param id The ID to assign to the widget
     */
    public UIText(String id) {
        super(id);
        cursorTexture = Assets.getTexture("engine:white").get();
    }

    /**
     * Handles how the widget is drawn.
     *
     * @param canvas The canvas on which the widget resides.
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (text.get() == null) {
            text.set("");
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

    /**
     * Draws the selection indication which indicates that a certain part of the text is selected.
     *
     * @param canvas The canvas on which the widget resides
     */
    protected void drawSelection(Canvas canvas) {
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

    /**
     * Draws the cursor in the text field.
     *
     * @param canvas The canvas on which the widget resides
     */
    protected void drawCursor(Canvas canvas) {
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

    /**
     * Get the preferred content size of the widget.
     *
     * @param canvas   The canvas on which the widget resides
     * @param areaHint A suggestion for the preferred size of the widget
     * @return         The preferred content size of the widget
     */
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

    /**
     * Get the maximum content size of the widget.
     *
     * @param canvas The canvas on which the widget resides
     * @return       The maximum content size of the widget
     */
    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        Font font = canvas.getCurrentStyle().getFont();
        if (isMultiline()) {
            return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            return new Vector2i(Integer.MAX_VALUE, font.getLineHeight());
        }
    }

    /**
     * Handles what to do when a key is pressed while the text box is active.
     *
     * @param event The event corresponding to the key being pressed
     * @return      Whether the event was handled successfully or not.
     */
    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        correctCursor();
        boolean eventHandled = false;
        if (isEnabled() && event.isDown() && lastFont != null) {
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

    /**
     * Updates the cursor offset.
     */
    protected void updateOffset() {
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

    /**
     * Checks whether the keyboard modifier for text selection (Shift) is being used.
     *
     * @param keyboard A reference to the active keyboard device
     * @return         Whether the keyboard modifier for selection is active
     */
    protected boolean isSelectionModifierActive(KeyboardDevice keyboard) {
        return keyboard.isKeyDown(KeyId.LEFT_SHIFT) || keyboard.isKeyDown(KeyId.RIGHT_SHIFT);
    }

    /**
     * Check whether any part of the text in the text box is currently selected.
     *
     * @return Whether any part of the text is currently selected
     */
    protected boolean hasSelection() {
        return getCursorPosition() != selectionStart;
    }

    /**
     * Removes the selected text from the text field.
     */
    protected void removeSelection() {
        if (hasSelection()) {
            String before = getText().substring(0, Math.min(getCursorPosition(), selectionStart));
            String after = getText().substring(Math.max(getCursorPosition(), selectionStart));
            setText(before + after);
            setCursorPosition(Math.min(getCursorPosition(), selectionStart));
        }
    }

    /**
     * Copies the selected text to the clipboard.
     */
    protected void copySelection() {
        if (hasSelection()) {
            String fullText = getText();
            String selection = fullText.substring(Math.min(selectionStart, getCursorPosition()), Math.max(selectionStart, getCursorPosition()));
            setClipboardContents(FontUnderline.strip(FontColor.stripColor(selection)));
        }
    }

    /**
     * Pastes the text currently in the clipboard.
     */
    protected void paste() {
        String fullText = getText();
        String before = fullText.substring(0, getCursorPosition());
        String after = fullText.substring(getCursorPosition());
        String pasted = getClipboardContents();
        setText(before + pasted + after);
        increaseCursorPosition(pasted.length());
    }

    /**
     * Get the current clipboard contents.
     *
     * @return The string currently in the clipboard
     */
    protected String getClipboardContents() {
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

    /**
     * Set the contents of the clipboard to a given value.
     *
     * @param str The new value of the clipboard contents
     */
    protected void setClipboardContents(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    }

    /**
     * Moves the cursor to a given position.
     *
     * @param pos       The final position of the cursor
     * @param selecting Whether the user is selecting text as he moves the cursor
     * @param keyboard  The keyboard device that is currently active
     */
    protected void moveCursor(Vector2i pos, boolean selecting, KeyboardDevice keyboard) {
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

    /**
     * Change the binding associated with the text in the widget.
     *
     * @param binding The new binding to associate with the text in the widget
     */
    public void bindText(Binding<String> binding) {
        text = binding;
    }

    /**
     * Get the text contained by the text box.
     *
     * @return The text contained by the text box
     */
    public String getText() {
        return text.get();
    }

    /**
     * Set the text in the text box to a given value.
     *
     * @param val The new value of the text in the widget
     */
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

    /**
     * Get the mode (default or disabled) of the text box.
     *
     * @return The String ID associated with the mode in which the text box currently is
     */
    @Override
    public String getMode() {
        if (!isEnabled()) {
            return DISABLED_MODE;
        }
        return DEFAULT_MODE;
    }

    /**
     * @return Whether the text in the text box is multiline
     */
    public boolean isMultiline() {
        return multiline;
    }

    /**
     * @param multiline Whether the text in the text box should be multiline
     */
    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    /**
     * @return Whether the text box is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @param readOnly Whether the text box should be read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Add a new activate event listener to the widget.
     *
     * @param listener The activate event listener to add to the widget.
     */
    public void subscribe(ActivateEventListener listener) {
        Preconditions.checkNotNull(listener);
        activationListeners.add(listener);
    }

    /**
     * Remove a new activate event listener from the widget.
     *
     * @param listener The activate event listener to remove from the widget.
     */
    public void unsubscribe(ActivateEventListener listener) {
        Preconditions.checkNotNull(listener);
        activationListeners.remove(listener);
    }

    /**
     * Add a new cursor update event listener to the widget.
     *
     * @param listener The cursor update event listener to add to the widget.
     */
    public void subscribe(CursorUpdateEventListener listener) {
        Preconditions.checkNotNull(listener);
        cursorUpdateListeners.add(listener);
    }

    /**
     * Remove a new cursor update event listener from the widget.
     *
     * @param listener The cursor update event listener to remove from the widget.
     */
    public void unsubscribe(CursorUpdateEventListener listener) {
        Preconditions.checkNotNull(listener);
        cursorUpdateListeners.remove(listener);
    }

    /**
     * Add a new text change event listener to the widget.
     *
     * @param listener The text change event listener to add to the widget.
     */
    public void subscribe(TextChangeEventListener listener) {
        Preconditions.checkNotNull(listener);
        textChangeListeners.add(listener);
    }

    /**
     * Remove a new text change event listener from the widget.
     *
     * @param listener The text change event listener to remove from the widget.
     */
    public void unsubscribe(TextChangeEventListener listener) {
        Preconditions.checkNotNull(listener);
        textChangeListeners.remove(listener);
    }

    /**
     * Defines what to do at every engine update. Specifically, this updates the text and makes the cursor blink.
     *
     * @param delta
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        blinkCounter += delta;
        while (blinkCounter > 2 * BLINK_RATE) {
            blinkCounter -= 2 * BLINK_RATE;
        }
    }

    /**
     * Increase the cursor position.
     *
     * @param delta              The amount by which the cursor position is to be increased
     * @param moveSelectionStart Whether the start of the selected text should be moved with the cursor
     * @return                   The new position of the cursor
     */
    public int increaseCursorPosition(int delta, boolean moveSelectionStart) {
        int newPosition = getCursorPosition() + delta;

        setCursorPosition(newPosition, moveSelectionStart);

        return newPosition;
    }

    /**
     * Increase the cursor position. This method moves the start of the selected text along with the cursor.
     *
     * @param delta The amount by which the cursor position is to be increased
     * @return      The new position of the cursor
     */
    public int increaseCursorPosition(int delta) {
        return increaseCursorPosition(delta, true);
    }

    /**
     * Decrease the cursor position.
     *
     * @param delta              The amount by which the cursor position is to be decreased
     * @param moveSelectionStart Whether the start of the selected text should be moved with the cursor
     * @return                   The new position of the cursor
     */
    public int decreaseCursorPosition(int delta, boolean moveSelectionStart) {
        return increaseCursorPosition(-delta, moveSelectionStart);
    }

    /**
     * Decrease the cursor position. This method moves the start of the selected text along with the cursor.
     *
     * @param delta The amount by which the cursor position is to be decreased
     * @return      The new position of the cursor
     */
    public int decreaseCursorPosition(int delta) {
        return decreaseCursorPosition(delta, true);
    }

    /**
     * @return The current cursor position
     */
    public int getCursorPosition() {
        return cursorPosition;
    }

    /**
     * @param position The new cursor position
     */
    public void setCursorPosition(int position) {
        setCursorPosition(position, true, true);
    }

    /**
     * @param position           The new cursor position
     * @param moveSelectionStart Whether the start of the selected text should be moved with the cursor
     * @param callEvent          Whether this action should be reported as an event
     */
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

    /**
     * @param position           The new cursor position
     * @param moveSelectionStart Whether the start of the selected text should be moved with the cursor
     */
    public void setCursorPosition(int position, boolean moveSelectionStart) {
        setCursorPosition(position, moveSelectionStart, true);
    }

    /**
     * Make sure that the cursor position lies within 0 and the length of the text in the widget.
     */
    protected void correctCursor() {
        cursorPosition = TeraMath.clamp(cursorPosition, 0, getText().length());
        selectionStart = TeraMath.clamp(selectionStart, 0, getText().length());
    }
}
