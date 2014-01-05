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
package org.terasology.rendering.gui.widgets;

import com.google.common.collect.Lists;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Keyboard;
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.FocusListener;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.framework.style.StyleShadow.EShadowDirection;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

/**
 * A text area which can be used as a single line input box, multi line input box or for just displaying large texts.
 * The content is scrollable and it also supports text wrapping in multi line mode. Moreover it supports the usual text editing, like selecting text and copy & paste.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         <p/>
 *         TODO remove text wrapping, the UILabel widget can do this.
 *         TODO clean up
 */
public class UIText extends UIDisplayContainerScrollable {
    
    /* 
       1. Basic explanation
       
        In the following methods there always needs to be discerned between the 'display position' and the 'text position'. The 'display position' 
        is an actual position on the display where things get drawn. The 'text position' is an index of the string which is displayed. The methods 
        'toDisplayPosition' and 'toTextPosition' can convert one position into the other.
        
        The 'toDisplayPosition' method simply uses the 'calcTextHeight' and 'calcTextWidth' methods to calculate the display position.
        The 'toTextPosition' also uses 'calcTextHeight' and 'calcTextWidth' to calculate the text position by looping through the whole, 
        or just to a part of the text which is displayed and checks if the right position is reached.
       
       2. Explanation of text wrapping
       
        Text wrapping will be done by simply insert a new line character if the text is to long. These inserted new line characters will be saved 
        in the 'wrapPosition' list by there position in the text. By using the 'getText' method these fake line breaks will be removed to get the 
        original text.

       ============================================================================================================================================
       
       Note 1: Calculating the text width and height by using the slick library isn't a perfect solution. Currently the cursor will be slightly 
               off the proper position at some characters. There is room for improvement there to calculate the >correct< text size. Thats why there 
               are a few workarounds in the 'calcTextHeight' method.

       Note 2: There is some dividing by the factor of 2 going on when setting the position or size of the character. This needs to be done,
               otherwise the cursor will be at the wrong position. I can't explain this behavior.
    */

    private static final Logger logger = LoggerFactory.getLogger(UIText.class);

    //events
    private final List<ChangedListener> changedListeners = Lists.newArrayList();
    private final List<SelectionListener> selectionListeners = Lists.newArrayList();

    //wrapping
    private final List<Integer> wrapPosition = Lists.newArrayList();

    //selection
    private int cursorPosition = -1;
    private boolean selection;
    private int selectionStart;
    private int selectionEnd;

    //characters
    private final char[] specialCharacters = new char[]{' ', '_', '.', ',', '/', '!', '-', '(', ')', '"', '\'', ';', ':', '+'};
    private final char[] multiLineSecialCharacters = new char[]{'\n'};
    private boolean ctrlKeyPressed;

    //options
    private final Vector2f cursorSize = new Vector2f(1f, 16f);
    private int maxLength;
    private boolean disabled;
    private boolean multiLine;

    //child elements
    private final UILabel text;
    private final UITextCursor cursor;
    private final UISelection selectionRectangle;

    //key listener
    private KeyListener keyListener = new KeyListener() {
        @Override
        public void key(UIDisplayElement element, KeyEvent event) {
            if (isFocused()) {
                //delete
                if (event.getKey() == Keyboard.Key.BACKSPACE && event.isDown()) {
                    if (text.getText().length() > 0) {
                        //delete selection
                        if (selectionStart != selectionEnd) {
                            deleteSelection();
                        } else if (cursorPosition > 0) {
                            //delete at cursor position
                            int pos = getWrapOffset(cursorPosition);
                            replaceText(pos - 1, pos, "");

                            setCursorToTextPosition(cursorPosition - 1);
                        }
                    }

                    event.consume();
                }
                //delete
                if (event.getKey() == Keyboard.Key.DELETE && event.isDown()) {
                    if (text.getText().length() > 0) {
                        //delete selection
                        if (selectionStart != selectionEnd) {
                            deleteSelection();
                        } else if (cursorPosition < text.getText().length()) {
                            //delete at cursor position
                            int pos = getWrapOffset(cursorPosition);
                            replaceText(pos, pos + 1, "");

                            setCursorToTextPosition(cursorPosition);
                        }
                    }

                    event.consume();
                } else if (event.getKey() == Keyboard.Key.HOME && event.isDown()) {
                    clearSelection();
                    setCursorToTextPosition(0);
                    event.consume();
                } else if (event.getKey() == Keyboard.Key.END && event.isDown()) {
                    clearSelection();
                    setCursorToTextPosition(text.getText().length());
                    event.consume();
                } else if (event.getKey() == Keyboard.Key.LEFT && event.isDown()) {
                    //move cursor left
                    clearSelection();
                    if (ctrlKeyPressed) {
                        setCursorToTextPosition(findNextChar(cursorPosition, ' ', true));
                    } else {
                        setCursorToTextPosition(cursorPosition - 1);
                    }

                    event.consume();
                } else if (event.getKey() == Keyboard.Key.RIGHT && event.isDown()) {
                    //move cursor right
                    clearSelection();
                    if (ctrlKeyPressed) {
                        setCursorToTextPosition(findNextChar(cursorPosition, ' ', false));
                    } else {
                        setCursorToTextPosition(cursorPosition + 1);
                    }

                    event.consume();
                } else if (event.getKey() == Keyboard.Key.UP && event.isDown()) {
                    //move cursor up
                    //TODO better solution here the behavior is kinda wrong
                    clearSelection();

                    Vector2f cursorPos = cursor.getPosition();
                    setCursorToTextPosition(toTextPositionLocal(new Vector2f(cursorPos.x, cursorPos.y - cursor.getSize().y + 1)));

                    event.consume();
                } else if (event.getKey() == Keyboard.Key.DOWN && event.isDown()) {
                    //move cursor down
                    //TODO better solution here the behavior is kinda wrong
                    clearSelection();

                    Vector2f cursorPos = cursor.getPosition();
                    setCursorToTextPosition(toTextPositionLocal(new Vector2f(cursorPos.x, cursorPos.y + cursor.getSize().y + 1)));

                    event.consume();
                } else if (event.getKey() == Keyboard.Key.LEFT_CTRL || event.getKey() == Keyboard.Key.RIGHT_CTRL) {
                    //left/right control pressed
                    ctrlKeyPressed = event.isDown();

                    event.consume();
                } else if (ctrlKeyPressed && event.getKey() == Keyboard.Key.X && event.isDown()) {
                    //cut selection
                    cut();

                    event.consume();
                } else if (ctrlKeyPressed && event.getKey() == Keyboard.Key.C && event.isDown()) {
                    //copy selection
                    copy();

                    event.consume();
                } else if (ctrlKeyPressed && event.getKey() == Keyboard.Key.V && event.isDown()) {
                    //paste selection
                    paste();

                    event.consume();
                } else if (ctrlKeyPressed && event.getKey() == Keyboard.Key.A && event.isDown()) {
                    //select all
                    setSelection(0, text.getText().length());

                    event.consume();
                } else if (event.isDown()) {
                    //add character
                    char c = event.getKeyCharacter();

                    if (c == '\r') {
                        c = '\n';
                    }

                    if (validateChar(c)) {
                        //delete selection
                        if (selectionStart != selectionEnd) {
                            deleteSelection();
                        }

                        int num = insertText(getWrapOffset(cursorPosition), String.valueOf(c));

                        setCursorToTextPosition(cursorPosition + num);
                        event.consume();
                    }

                }
            }
        }
    };


    public UIText() {
        //key listener for processing the input
        addKeyListener(keyListener);

        //mouse button listener to detect mouse clicks on the text and moving the cursor
        addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                //end the selection
                selection = false;
                if (intersect && !isDisabled()) {
                    selectionEnd = toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                    setCursorToTextPosition(toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY())));
                }
            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                //start the selection
                if (intersect && !isDisabled()) {
                    selection = true;
                    selectionStart = toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                    selectionEnd = selectionStart;

                    selectionRectangle.updateSelection(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd));
                    setCursorToTextPosition(toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY())));
                    setFocus(UIText.this);
                }
            }
        });

        //mouse move listener to update position of cursor if mouse is pressed
        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void move(UIDisplayElement element) {
                if (selection) {
                    selectionEnd = toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                    setCursorToTextPosition(selectionEnd);
                }
            }

            @Override
            public void leave(UIDisplayElement element) {

            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {

            }
        });

        //update of the selection rectangle
        addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                selectionRectangle.updateSelection(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd));
            }
        });

        //focus listener for hiding/displaying the cursor
        addFocusListener(new FocusListener() {
            @Override
            public void focusOn(UIDisplayElement element) {
                if (!isDisabled()) {
                    cursor.setVisible(true);
                    selectionRectangle.setFade(false);
                    setCursorPosition(cursorPosition);
                }
            }

            @Override
            public void focusOff(UIDisplayElement element) {
                cursor.setVisible(false);
                selectionRectangle.setFade(true);
            }
        });

        text = new UILabel();
        text.setVerticalAlign(EVerticalAlign.CENTER);
        text.setColor(Color.black);
        text.setVisible(true);

        cursor = new UITextCursor(cursorSize);
        selectionRectangle = new UISelection();

        addDisplayElement(text);
        text.addDisplayElement(selectionRectangle);
        text.addDisplayElement(cursor);

        setPadding(new Vector4f(0f, 5f, 0f, 5f));
        setBackgroundColor(new Color(255, 255, 255));
        setShadow(new Vector4f(0f, 4f, 4f, 0f), EShadowDirection.OUTSIDE, 1f);
        setBorderSolid(new Vector4f(1f, 1f, 1f, 1f), new Color(0, 0, 0));
        setMultiLine(false);
    }

    /**
     * Set the cursor position to the given position in the text.
     *
     * @param index The text position (index) to set the cursor to.
     */
    private void setCursorToTextPosition(int index) {
        int clampedIndex = index;
        if (clampedIndex < 0) {
            clampedIndex = 0;
        } else if (clampedIndex > text.getText().length()) {
            clampedIndex = text.getText().length();
        }

        //calculate the display position of the cursor from text position
        if (clampedIndex != cursorPosition) {
            Vector2f newPos = toDisplayPosition(clampedIndex);
            newPos.y -= cursorSize.y;
            cursor.setPosition(newPos);

            if (isMultiLine()) {
                //cursor is below of the input area
                if ((cursor.getSize().y + cursor.getPosition().y - getScrollPosition()) > (getSize().y)) {
                    float additionalSpacing = getPadding().z + 5f;
                    scrollTo(cursor.getPosition().y * 2 + cursor.getSize().y - getSize().y + additionalSpacing);
                } else if (cursor.getPosition().y - getScrollPosition() < 0) {
                    //cursor is on top of the input area
                    float additionalSpacing = getPadding().x + 5f;
                    scrollTo(cursor.getPosition().y - additionalSpacing);
                }
            } else {
                //cursor is right from input area
                if ((cursor.getPosition().x + text.getPosition().x + getPadding().y) > (getSize().x)) {
                    text.setPosition(new Vector2f(-(cursor.getPosition().x - getSize().x + getPadding().y + getPadding().w), 0f));
                } else if ((cursor.getPosition().x + text.getPosition().x) < 0) {
                    //cursor is left from input area
                    text.setPosition(new Vector2f(-cursor.getPosition().x, 0f));
                }
            }

            cursorPosition = clampedIndex;

            notifySelectionListeners();
        }
    }

    /**
     * Get the cursor position at the given display position.
     *
     * @return Returns the text position (index) of the cursor.
     */
    private int toTextPosition(Vector2f mousePos) {
        Vector2f textAbsPos = text.getAbsolutePosition();
        Vector2f clampedMousePos = new Vector2f(Math.max(mousePos.x, textAbsPos.x), Math.max(mousePos.y, textAbsPos.y));
        Vector2f relative = new Vector2f(clampedMousePos.x - textAbsPos.x, clampedMousePos.y - textAbsPos.y);

        //multi line
        //if (isMultiLine()) {
        //relative.x = relative.x - getPadding().y - getPadding().w;
        //clicked bottom of text container
        if (clampedMousePos.y >= (textAbsPos.y + getSize().y + getScrollPosition())) {
            return text.getText().length();
        } else if (clampedMousePos.y <= textAbsPos.y) {
            //clicked top of text container
            return 0;
        } else {
            return toTextPositionLocal(relative);
        }
    }

    private int toTextPositionLocal(Vector2f local) {
        int charIndex = 0;
        while (charIndex < text.getText().length()) {
            //first calculate the height
            if (calcTextHeight(text.getText().substring(0, charIndex)) >= local.y) {
                //clicked left from the text box
                if (local.x <= 0) {
                    return charIndex;
                }

                //clicked somewhere in the text or right from text
                for (int j = charIndex; j < text.getText().length(); j++) {
                    //than calculate the width
                    if (calcTextWidth(text.getText().substring(charIndex, j)) > local.x || text.getText().charAt(j) == '\n') {
                        return j;
                    } else if (j == text.getText().length() - 1) {
                        return j + 1;
                    }
                }
            }

            charIndex = findNextChar(charIndex, '\n', false);
        }
        return text.getText().length();
    }

    /**
     * Get the position on the display at the given position in the text.
     *
     * @param index The position in the text. From 0 to the length of the text.
     * @return Returns the display position.
     */
    private Vector2f toDisplayPosition(int index) {
        Vector2f displayPos = new Vector2f();

        String substr = text.getText().substring(0, index);
        String lastLine = substr;

        int indexLastLine = substr.lastIndexOf('\n');
        if (indexLastLine != -1) {
            lastLine = substr.substring(indexLastLine, substr.length());
        }

        displayPos.x = calcTextWidth(lastLine);
        displayPos.y = calcTextHeight(substr);

        return displayPos;
    }

    /**
     * Calculate the width of the given text.
     *
     * @param string The text to calculate the width.
     * @return Returns the width of the given text.
     */
    private int calcTextWidth(String string) {
        return this.text.getFont().getWidth(string);
    }

    /**
     * Calculate the height of the given text.
     *
     * @param string The text to calculate the height.
     * @return Returns the height of the given text.
     */
    private int calcTextHeight(String string) {
        return this.text.getFont().getHeight(string);
    }

    /**
     * Remove all unsupported characters from a string.
     *
     * @param string The string to remove all unsupported characters.
     * @return Returns the string where all unsupported characters are removed.
     */
    private String removeUnsupportedChars(String string) {
        StringBuilder original = new StringBuilder(string);
        StringBuilder replaced = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            if (validateChar(original.charAt(i))) {
                replaced.append(original.charAt(i));
            }
        }

        return replaced.toString();
    }

    /**
     * Validate a character.
     *
     * @param c The character to validate.
     * @return Returns true if the character is valid.
     */
    private boolean validateChar(char c) {
        boolean valid = false;

        if (c >= 'a' && c < 'z' + 1) {
            valid = true;
        } else if (c >= 'A' && c < 'Z' + 1) {
            valid = true;
        } else if (c >= '0' && c < '9' + 1) {
            valid = true;
        } else {
            if (isMultiLine()) {
                for (char ch : multiLineSecialCharacters) {
                    if (ch == c) {
                        valid = true;

                        break;
                    }
                }
            }

            for (char ch : specialCharacters) {
                if (ch == c) {
                    valid = true;

                    break;
                }
            }
        }

        return valid;
    }

    /**
     * Get the next or previous space in the text from the current cursor position.
     *
     * @param reverse True to search backwards.
     * @return Returns the text position (index) where the next or previous space was found.
     */
    private int findNextChar(int index, char ch, boolean reverse) {
        StringBuilder str = new StringBuilder(text.getText());

        //backwards search
        if (reverse) {
            for (int i = index - 1; i >= 0; i--) {
                if (str.charAt(i) == ch && i != index - 1) {
                    return i + 1;
                } else if (i == 0) {
                    return i;
                }
            }
        } else {
            //forward search
            for (int i = index; i < str.length(); i++) {
                if (str.charAt(i) == ch || i == (str.length() - 1)) {
                    return i + 1;
                }
            }
        }

        return index;
    }

    /**
     * Set the text in the clipboard.
     *
     * @param str The text to set.
     */
    private void setClipboard(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    }

    /**
     * Get the text from the clipboard.
     *
     * @return Returns the text from the clipboard.
     */
    private String getClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String str = (String) t.getTransferData(DataFlavor.stringFlavor);
                return str;
            }
        } catch (UnsupportedFlavorException | IOException e) {
            logger.warn("Failed to get data from clipboard", e);
        }

        return "";
    }

    /**
     * Wraps the text to the size of the container.
     *
     * @param string The text to wrap.
     * @return Returns the wrapped text.
     */
    private String wrapText(String string) {
        //multi line
        if (isMultiLine()) {
            int lastSpace = 0;
            int lastWrap = 0;
            StringBuilder wrapText = new StringBuilder(string + " ");

            wrapPosition.clear();

            //loop through whole text
            for (int i = 0; i < wrapText.length(); i++) {

                //check if character is a space -> text can only be wrapped at spaces
                if (wrapText.charAt(i) == ' ') {
                    //check if the string (from the beginning of the new line) is bigger than the container width
                    if (calcTextWidth(wrapText.substring(lastWrap, i)) > getScrollContainerSize().x) {
                        //than wrap the text at the previous space
                        wrapText.insert(lastSpace + 1, '\n');
                        wrapPosition.add(lastSpace + 1);

                        lastWrap = lastSpace + 1;
                    }

                    lastSpace = i;
                } else if (wrapText.charAt(i) == '\n') {
                    lastSpace = i;
                    lastWrap = i;
                }
            }

            wrapText.replace(wrapText.length() - 1, wrapText.length(), "");

            return wrapText.toString();
        } else {
            //single line
            return string;
        }
    }

    /**
     * Get the offset in text length which happened due to the additional new line characters at text wrapping.
     *
     * @param index The offset will be calculated to this index.
     * @return Returns the offset in text length.
     */
    private int getWrapOffset(int index) {
        for (int i = 0; i < wrapPosition.size(); i++) {
            if (wrapPosition.get(i) >= index) {
                return index - i;
            } else if (i == wrapPosition.size() - 1) {
                return index - i - 1;
            }
        }

        return index;
    }

    /**
     * Get the displayed text.
     *
     * @return Returns the text.
     */
    public String getText() {
        StringBuilder str = new StringBuilder(text.getText());
        for (int i = 0; i < wrapPosition.size(); i++) {
            str.replace(wrapPosition.get(i) - i, wrapPosition.get(i) + 1 - i, "");
        }

        return str.toString();
    }

    /**
     * Set the text of the label.
     *
     * @param value The text to set.
     */
    public int setText(String value) {
        boolean scrollbarVisibility = isScrollbarVisible();

        String clampedString = value;
        //check the max string length
        if (maxLength > 0) {
            if (value.length() > maxLength) {
                clampedString = value.substring(0, maxLength);
            }
        }

        this.text.setText(wrapText(clampedString));

        calcContentHeight();

        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(getText());
        }

        notifyChangedListeners();

        return clampedString.length();
    }

    /**
     * Append a text to the current displayed text.
     *
     * @param string The text to append.
     * @return Returns the number of characters which where added.
     */
    public int appendText(String string) {
        boolean scrollbarVisibility = isScrollbarVisible();

        String str = getText();

        String clampedText = string;
        //check the max string length
        if (maxLength > 0) {
            if (str.length() >= maxLength) {
                return 0;
            }

            if (str.length() + string.length() > maxLength) {
                clampedText = string.substring(0, maxLength - str.length());
            }
        }

        setText(str + clampedText);

        calcContentHeight();

        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(getText());
        }

        return clampedText.length();
    }

    /**
     * Insert a text at a specific position into the current displayed text of the label.
     *
     * @param offset The offset, where to insert the text at.
     * @param string The text to insert.
     * @return Returns the number of characters which where added.
     */
    public int insertText(int offset, String string) {

        int prevWraps = getWrapOffset(cursorPosition);
        int currentWraps;
        int diff;

        boolean scrollbarVisibility = isScrollbarVisible();

        StringBuilder str = new StringBuilder(getText());

        String clampedString = string;
        //check the max string length
        if (maxLength > 0) {
            if (str.length() >= maxLength) {
                return 0;
            }

            if (str.length() + string.length() > maxLength) {
                clampedString = string.substring(0, maxLength - str.length());
            }
        }

        setText(str.insert(offset, clampedString).toString());

        calcContentHeight();

        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(getText());
        }

        //if text is inserted, the cursor might night a shift, because additional new lines could be inserted by wrapping the text
        currentWraps = getWrapOffset(cursorPosition);
        diff = currentWraps - prevWraps;
        cursorPosition = cursorPosition - diff;

        return clampedString.length();
    }

    /**
     * Delete the whole text.
     */
    public void deleteText() {
        setText("");
        clearSelection();
        setCursorToTextPosition(0);
    }

    /**
     * Replace a string defined by its start and end index.
     *
     * @param start   The start index.
     * @param end     The end index.
     * @param newText The text to replace with.
     */
    public void replaceText(int start, int end, String newText) {

        boolean scrollbarVisibility = isScrollbarVisible();

        StringBuilder str = new StringBuilder(getText());
        setText(str.replace(start, end, newText).toString());

        calcContentHeight();

        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(getText());
        }
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int position) {
        setCursorToTextPosition(position);
    }

    public void setCursorStart() {
        setCursorToTextPosition(0);
    }

    public void setCursorEnd() {
        setCursorToTextPosition(text.getText().length());
    }

    /**
     * Sets the selection.
     *
     * @param start The start of the selection.
     */
    public void setSelection(int start) {
        selectionStart = start;
        selectionEnd = getText().length();

        selectionRectangle.updateSelection(start, selectionEnd);

        setCursorToTextPosition(selectionEnd);

        notifySelectionListeners();
    }

    /**
     * Sets the selection to the range specified by the given start and end indices.
     *
     * @param start The start index of the selection.
     * @param end   The end index of the selection.
     */
    public void setSelection(int start, int end) {
        selectionStart = start;
        selectionEnd = end;

        selectionRectangle.updateSelection(start, end);

        setCursorToTextPosition(selectionEnd);

        notifySelectionListeners();
    }

    /**
     * Get the selected text.
     *
     * @return Returns the selected text. If no text is selected it will return an empty string.
     */
    public String getSelection() {
        if (selectionStart != selectionEnd) {
            //TODO probably not correct. need to get the original string by getText() -> havn't tested it
            return text.getText().substring(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd));
        }

        return "";
    }

    /**
     * Reset the selection.
     */
    public void clearSelection() {
        selectionStart = 0;
        selectionEnd = 0;

        selectionRectangle.updateSelection(selectionStart, selectionEnd);

        notifySelectionListeners();
    }

    /**
     * Delete the selected text. This has no effect if no text is selected.
     */
    public void deleteSelection() {
        if (selectionStart != selectionEnd) {
            replaceText(Math.min(getWrapOffset(selectionStart), getWrapOffset(selectionEnd)), Math.max(getWrapOffset(selectionStart), getWrapOffset(selectionEnd)), "");
            int cursorTo = Math.min(selectionStart, selectionEnd);
            selectionEnd = selectionStart;
            selectionRectangle.updateSelection(selectionStart, selectionEnd);
            setCursorToTextPosition(cursorTo);

            notifySelectionListeners();
        }
    }

    /**
     * Copy the selection.
     */
    public void copy() {
        setClipboard(getSelection());
    }

    /**
     * Cut the selection.
     */
    public void cut() {
        setClipboard(getSelection());
        deleteSelection();
    }

    /**
     * Pastes text from clipboard.
     */
    public void paste() {
        if (selectionStart != selectionEnd) {
            deleteSelection();
        }

        String clipboard = removeUnsupportedChars(getClipboard());

        int num = insertText(getWrapOffset(cursorPosition), clipboard);

        setCursorToTextPosition(cursorPosition + num);
    }

    /**
     * Get the text color.
     *
     * @return Returns the text color.
     */
    public Color getColor() {
        return text.getColor();
    }

    /**
     * Set the text color.
     *
     * @param color The color to set.
     */
    public void setColor(Color color) {
        text.setColor(color);
        cursor.setColor(color);
    }


    /**
     * Set the text selection color.
     *
     * @param color The text selection color to set.
     */

    public void setSelectionColor(Color color) {
        selectionRectangle.setColor(color);
    }

    /**
     * Get the text selection color.
     *
     * @return Returns the text selection color.
     */
    public Color getSelectionColor() {
        return selectionRectangle.getColor();
    }

    /**
     * Get the shadow color.
     *
     * @return Returns the shadow color.
     */
    public Color getShadowColor() {
        return text.getTextShadowColor();
    }

    /**
     * Set the shadow color.
     *
     * @param shadowColor The shadow color to set.
     */
    public void setShadowColor(Color shadowColor) {
        text.setTextShadowColor(shadowColor);
    }

    /**
     * Check whether the text has a shadow.
     *
     * @return Returns true if the text has a shadow.
     */
    public boolean isEnableShadow() {
        return this.text.isTextShadow();
    }

    /**
     * Set whether the text has a color.
     *
     * @param enable True to enable the shadow of the text.
     */
    public void setEnableShadow(boolean enable) {
        this.text.setTextShadow(enable);
    }

    /**
     * Get the font.
     *
     * @return Returns the font.
     */
    public Font getFont() {
        return text.getFont();
    }

    /**
     * Set the font.
     *
     * @param font The font to set.
     */
    public void setFont(Font font) {
        text.setFont(font);
    }

    /**
     * Check whether the element is disabled. There can't be written anything in a disabled element.
     *
     * @return Returns true if the element is disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Get the maximum of characters the text element can hold.
     *
     * @return Returns the maximum characters.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Set the maximum of characters the text element can hold.
     *
     * @param maxLength The maximum characters.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Check whether the text element is a multi line text element.
     *
     * @return Returns true if the text element is a multi line text element.
     */
    public boolean isMultiLine() {
        return multiLine;
    }

    /**
     * Set whether the text element is a multi line text element.
     *
     * @param enable True to enable multi line support.
     */
    public void setMultiLine(boolean enable) {
        this.multiLine = enable;

        if (enable) {
            setEnableScrolling(true);
            setEnableScrollbar(true);
            text.setVerticalAlign(EVerticalAlign.TOP);
        } else {
            setEnableScrolling(false);
            setEnableScrollbar(false);
            setCropContainer(true);
            text.setVerticalAlign(EVerticalAlign.CENTER);
            setText(getText().replaceAll("\n", ""));
        }
    }

    /**
     * Set whether the element is disabled. There can't be written anything in a disabled element.
     *
     * @param disabled True to disable the element.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;

        if (disabled) {
            setFocus(null);
            cursor.setVisible(false);
            clearSelection();
            selectionRectangle.setVisible(false);
        }
    }
    
    /*
       Event listeners
    */

    private void notifySelectionListeners() {
        for (SelectionListener listener : selectionListeners) {
            listener.changed(this);
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(listener);
    }

    private void notifyChangedListeners() {
        for (ChangedListener listener : changedListeners) {
            listener.changed(this);
        }
    }

    public void addChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void removeChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }

    /**
     * A text cursor.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    private class UITextCursor extends UIDisplayContainer {
        private Color color = new Color(Color.black);

        public UITextCursor(Vector2f size) {
            setSize(size);
        }

        public void render() {
            if (!isVisible()) {
                return;
            }

            glPushMatrix();
            glColor4f(color.r, color.g, color.b, color.a);
            glLineWidth(getSize().x);
            glBegin(GL11.GL_LINES);
            glVertex2f(0, 0);
            glVertex2f(0, getSize().y);
            glEnd();
            glPopMatrix();
        }

        public void update() {

        }

        public void setColor(Color color) {
            this.color = color;
        }
    }

    /**
     * The selection rectangle.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    private class UISelection extends UIDisplayContainer {
        private Color color = new Color(Color.gray);
        private final List<Vector2f[]> rectangles = new ArrayList<Vector2f[]>();
        private boolean fade;

        public void render() {
            if (!isVisible() || rectangles.size() == 0) {
                return;
            }

            glPushMatrix();

            float alpha = color.a;
            if (fade) {

                alpha = Math.max(0, alpha - 0.5f);
            }
            glColor4f(color.r, color.g, color.b, alpha);

            glBegin(GL_QUADS);

            for (Vector2f[] rect : rectangles) {
                //[0] = position, [1] = size
                glVertex2f(rect[0].x, rect[0].y);
                glVertex2f(rect[0].x + rect[1].x, rect[0].y);
                glVertex2f(rect[0].x + rect[1].x, rect[0].y + rect[1].y);
                glVertex2f(rect[0].x, rect[0].y + rect[1].y);
            }

            glEnd();
            glPopMatrix();
        }

        public void update() {

        }

        public void updateSelection(int start, int end) {
            if (start != end) {
                rectangles.clear();

                //loop through all selected lines and add a selection rectangle for each
                int charIndex = start;
                while (charIndex < end) {
                    int nextLineStart = findNextChar(charIndex, '\n', false);
                    Vector2f pos = toDisplayPosition(charIndex);
                    pos.y -= cursor.getSize().y;
                    Vector2f size = new Vector2f(calcTextWidth(text.getText().substring(charIndex, Math.min(end, nextLineStart))), cursor.getSize().y);

                    rectangles.add(new Vector2f[]{pos, size});

                    charIndex = nextLineStart;
                }

                selectionRectangle.setVisible(true);
            } else {
                selectionRectangle.setVisible(false);
            }
        }

        public void setFade(boolean value) {
            this.fade = value;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }
}
