package org.terasology.rendering.gui.widgets;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.SelectionChangedListener;

/**
 * A one line input box which supports text highlighting and copy & paste.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIInputNew extends UIDisplayContainer {
    
    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    private final ArrayList<SelectionChangedListener> selectionListeners = new ArrayList<SelectionChangedListener>();
        
    //selection
    private int cursorPosition = -1;
    private boolean selection = false;
    private int selectionStart;
    private int selectionEnd;
    
    //keys
    private boolean ctrlKeyPressed = false;
    
    //child elements
    private final UIText text;
    private final UITextCursor cursor;
    private final UISelection selectionRectangle;
    
    //options
    private char[] specialCharacters = new char[] {' ', '_', '.', ',', '!', '-','(', ')', '"', '\'', ';', '+'};
    private final Vector2f cursorSize = new Vector2f(1f, 18f);
    private Vector2f padding = new Vector2f(5f, 5f);
    private boolean disabled;

    /**
     * A text cursor.
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     *
     */
    private class UITextCursor extends UIDisplayElement {
        private Color color = new Color(Color.black);

        public UITextCursor(Vector2f size){
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
            glVertex2f(getPosition().x, getPosition().y);
            glVertex2f(getPosition().x, getPosition().y + getSize().y);
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
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     *
     */
    private class UISelection extends UIDisplayElement {
        private Color color = new Color(Color.white);
        
        public void render() {
            if (!isVisible()) {
                return;
            }
            
            glPushMatrix();
            glColor4f(color.r, color.g, color.b, color.a);
            glBegin(GL_QUADS);
            glVertex2f(getPosition().x, getPosition().y);
            glVertex2f(getPosition().x + getSize().x, getPosition().y);
            glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
            glVertex2f(getPosition().x, getPosition().y + getSize().y);
            glEnd();
            glPopMatrix();
        }

        public void update() {

        }
        
        public void updateSelection(int start, int end) {
            if (start != end) {
                Vector2f startPos = toDisplayPosition(start);
                Vector2f endPos = toDisplayPosition(end);
                Vector2f size = new Vector2f(endPos.x - startPos.x, cursor.getSize().y);
                
                startPos.y = cursor.getPosition().y;
                startPos.x = startPos.x / 2;
                
                selectionRectangle.setPosition(startPos);
                selectionRectangle.setSize(size);
                selectionRectangle.setVisible(true);
            } else {
                selectionRectangle.setVisible(false);
            }
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
    }
    
    /**
     * Create a one line input box which supports text highlighting and copy & paste.
     * @param size The size of the element.
     */
    public UIInputNew(Vector2f size) {       
        setSize(size);
        setCropContainer(true);
        
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
                    setFocus(UIInputNew.this);
                }
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                //start the selection
                if (intersect && !isDisabled()) {
                    selection = true;
                    selectionStart = toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                    selectionEnd = selectionStart;
                    
                    selectionRectangle.updateSelection(selectionStart, selectionEnd);
                    setCursorToTextPosition(toTextPosition(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY())));
                }
            }
        });
        
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
        
        addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                selectionRectangle.updateSelection(selectionStart, selectionEnd);
            }
        });
        
        text = new UIText();
        text.setPosition(new Vector2f(5f, 0f));
        text.setVerticalAlign(EVerticalAlign.CENTER);
        text.setColor(Color.black);
        text.setVisible(true);
        
        cursor = new UITextCursor(cursorSize);
        cursor.setVisible(true);
        
        selectionRectangle = new UISelection();
        
        addDisplayElement(text);
        text.addDisplayElement(cursor);
        text.addDisplayElement(selectionRectangle);
        
        setCursorToTextPosition(cursorPosition);
    }
    
    @Override
    public void processKeyboardInput(KeyEvent event) {
        if (isFocused()) {
            //delete
            if (event.getKey() == Keyboard.KEY_BACK && event.isDown()) {
                if (text.getText().length() > 0) {
                    //delete selection
                    if (selectionStart != selectionEnd) {
                        deleteSelection();
                    }
                    //delete at cursor position
                    else if (cursorPosition > 0) {
                        text.replaceText(cursorPosition - 1, cursorPosition, "");
                    
                        setCursorToTextPosition(cursorPosition - 1);
                    }
                }
            }
            //delete
            if (event.getKey() == Keyboard.KEY_DELETE && event.isDown()) {
                if (text.getText().length() > 0) {
                    //delete selection
                    if (selectionStart != selectionEnd) {
                        deleteSelection();
                    }
                    //delete at cursor position
                    else if (cursorPosition < text.getText().length()) {
                        text.replaceText(cursorPosition, cursorPosition + 1, "");
                    
                        setCursorToTextPosition(cursorPosition);
                    }
                }
            }
            //move cursor left
            else if (event.getKey() == Keyboard.KEY_LEFT && event.isDown()) {
                if (ctrlKeyPressed) {
                    setCursorToTextPosition(getNextSpace(true));
                } else {
                    setCursorToTextPosition(cursorPosition - 1);
                }
            }
            //move cursor right
            else if (event.getKey() == Keyboard.KEY_RIGHT && event.isDown()) {
                if (ctrlKeyPressed) {
                    setCursorToTextPosition(getNextSpace(false));
                } else {
                    setCursorToTextPosition(cursorPosition + 1);
                }
            }
            //left/right control pressed
            else if (event.getKey() == Keyboard.KEY_LCONTROL || event.getKey() == Keyboard.KEY_RCONTROL) {
                ctrlKeyPressed = event.isDown();
            }
            //cut selection
            else if (ctrlKeyPressed && event.getKey() == Keyboard.KEY_X && event.isDown()) {
                setClipboard(getSelection());
                deleteSelection();
            }
            //copy selection
            else if (ctrlKeyPressed && event.getKey() == Keyboard.KEY_C && event.isDown()) {
                setClipboard(getSelection());
            }
            //paste selection
            else if (ctrlKeyPressed && event.getKey() == Keyboard.KEY_V && event.isDown()) {
                if (selectionStart != selectionEnd) {
                    deleteSelection();
                }
                
                String clipboard = removeUnsupportedChars(getClipboard());
                text.insertText(cursorPosition, clipboard);
                setCursorToTextPosition(cursorPosition + clipboard.length());
            }
            //select all
            else if (ctrlKeyPressed && event.getKey() == Keyboard.KEY_A && event.isDown()) {
                setSelection(0, text.getText().length());
            }
            //add character
            else if (event.isDown()) {
                char c = Keyboard.getEventCharacter();
                if (validateChar(c)) {
                    //delete selection
                    if (selectionStart != selectionEnd) {
                        deleteSelection();
                    }
                    
                    text.insertText(cursorPosition, String.valueOf(c));
                    
                    setCursorToTextPosition(cursorPosition + 1);
                }
            }
        }
    }
    
    /**
     * Set the cursor position to the given position in the text.
     * @param pos The text position to set the cursor to.
     */
    private void setCursorToTextPosition(int pos) {
        if (pos < 0) {
            pos = 0;
        } else if (pos > getText().length()) {
            pos = getText().length();
        }
        
        //calculate the display position of the cursor from text position
        if (pos != cursorPosition) {
            Vector2f newPos = new Vector2f((calcTextWidth(text.getText().substring(0, pos))) / 2f, -1f);
            cursor.setPosition(newPos);

            //cursor is right from input area
            if ((cursor.getPosition().x + text.getPosition().x / 2 + padding.y) > (getSize().x / 2f)) {
                moveText(-(cursor.getPosition().x * 2 - getSize().x + padding.y + padding.y));
            }
            //cursor is left from input area
            else if ((cursor.getPosition().x + text.getPosition().x / 2) < 0) {
                moveText(-cursor.getPosition().x * 2);
            }

            cursorPosition = pos;
            
            notifySelectionChangedListeners();
        }
    }

    /**
     * Get the cursor position at the current mouse position.
     * @return Returns the text position of the cursor.
     */
    private int toTextPosition(Vector2f mousePos) {
        Vector2f textAbsPos = text.getAbsolutePosition();
        mousePos = new Vector2f(Math.max(mousePos.x, textAbsPos.x), Math.max(mousePos.y, textAbsPos.y));
        Vector2f relative = new Vector2f(mousePos.x - textAbsPos.x, mousePos.y - textAbsPos.y);
        
        //clicked right from text
        if (mousePos.x >= (textAbsPos.x + text.getSize().x)) {
            return text.getText().length();
        }
        //clicked left from text
        else if (mousePos.x <= textAbsPos.x) {
            return 0;
        }
        //clicked somewhere on the text
        else {            
            //calculate the cursor position
            for (int i = 0; i <= text.getText().length(); i++) {
                if (calcTextWidth(text.getText().substring(0, i)) >= relative.x) {
                    return i;
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Get the position on the display at the given position in the text.
     * @param pos The position in the text. From 0 to the length of the text.
     * @return Returns the display position.
     */
    private Vector2f toDisplayPosition(int pos) {
        Vector2f displayPos = new Vector2f();
        
        displayPos.x = calcTextWidth(text.getText().substring(0, pos));
        
        return displayPos;
    }
    
    /**
     * Move the text to a specific location.
     * @param pos The position to move the text to.
     */
    private void moveText(float pos) {
        text.setPosition(new Vector2f(pos + padding.x, 0f));
    }

    /**
     * Calculate the width of the given text.
     * @param text The text to calculate the width.
     * @return Returns the width of the given text.
     */
    private int calcTextWidth(String text) {
        return this.text.getFont().getWidth(text);
    }
    
    /**
     * Remove all unsupported characters from a string.
     * @param text The string to remove all unsupported characters.
     * @return Returns the string where all unsupported characters are removed.
     */
    private String removeUnsupportedChars(String text) {
        StringBuilder original = new StringBuilder(text);
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
     * @param c The character to validate.
     * @return Returns true if the character is valid.
     */
    private boolean validateChar(char c) {
        boolean valid = false;
        
        if (c >= 'a' && c < 'z' + 1) {
            valid = true;
        } else if(c >= 'A' && c < 'Z' + 1) {
            valid = true;
        } else if (c >= '0' && c < '9' + 1) {
            valid = true;
        } else {
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
     * @param reverse True to search backwards.
     * @return Returns the text position where the next or previous space was found.
     */
    private int getNextSpace(boolean reverse) {
        StringBuilder str = new StringBuilder(text.getText());
        
        //backwards search
        if (reverse) {
            for (int i = cursorPosition - 1; i >= 0; i--) {
                if (str.charAt(i) == ' ' && i != cursorPosition - 1) {
                    return i + 1;
                } else if (i == 0) {
                    return i;
                }
            }
        }
        //forward search
        else {
            for (int i = cursorPosition; i < str.length(); i++) {
                if ((str.charAt(i) == ' ' && i != cursorPosition) || i == (str.length() - 1)) {
                    return i + 1;
                }
            }
        }
        
        return cursorPosition;
    }
    
    /**
     * Set the text in the clipboard.
     * @param str The text to set.
     */
    private void setClipboard(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    }
    
    /**
     * Get the text from the clipboard.
     * @return Returns the text from the clipboard.
     */
    private String getClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String)t.getTransferData(DataFlavor.stringFlavor);
                return text;
            }
        } 
        catch (UnsupportedFlavorException e) {
            
        } 
        catch (IOException e) {
            
        }
        
        return "";
    }
    
    /**
     * Get the displayed text.
     * @return Returns the text.
     */
    public String getText() {
        return text.getText();
    }
    
    /**
     * Set the text of the label.
     * @param text The text to set.
     */
    public void setText(String text) {
        this.text.setText(text);
        
        notifyChangedListeners();
    }
    
    /**
     * Append a text to the current displayed text.
     * @param text The text to append.
     */
    public void appendText(String text) {
        this.text.appendText(text);
        
        notifyChangedListeners();
    }
    
    /**
     * Insert a text at a specific position into the current displayed text of the label.
     * @param offset The offset, where to insert the text at.
     * @param text The text to insert.
     */
    public void insertText(int offset, String text) {
        this.text.insertText(offset, text);
        
        notifyChangedListeners();
    }
    
    /**
     * Select a text.
     * @param start The start index of the selection.
     * @param end The end index of the selection.
     */
    public void setSelection(int start, int end) {
        selectionStart = start;
        selectionEnd = end;
        
        selectionRectangle.updateSelection(start, end);
        
        notifySelectionChangedListeners();
    }
    
    /**
     * Get the selected text.
     * @return Returns the selected text. If no text is selected it will return an empty string.
     */
    public String getSelection() {
        if (selectionStart != selectionEnd) {
            return text.getText().substring(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd));
        }
        
        return "";
    }
    
    /**
     * Reset the selection.
     */
    public void resetSelection() {
        selectionStart = 0;
        selectionEnd = 0;
        
        notifySelectionChangedListeners();
    }
    
    /**
     * Delete the selected text. This has no effect if no text is selected.
     */
    public void deleteSelection() {
        if (selectionStart != selectionEnd) {
            text.replaceText(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd), "");
            selectionEnd = selectionStart;
            selectionRectangle.updateSelection(selectionStart, selectionEnd);
            setCursorToTextPosition(Math.min(selectionStart, selectionEnd));
            
            notifyChangedListeners();
            notifySelectionChangedListeners();
        }
    }
    
    /**
     * Get the text color.
     * @return Returns the text color.
     */
    public Color getColor() {
        return text.getColor();
    }
    
    /**
     * Set the text color.
     * @param color The color to set.
     */
    public void setColor(Color color) {
        text.setColor(color);
        cursor.setColor(color);
    }
    
    /**
     * Get the text selection color.
     * @return Returns the text selection color.
     */
    public void setSelectionColor(Color color) {
        selectionRectangle.setColor(color);
    }
    
    /**
     * Set the text selection color.
     * @param color The text selection color to set.
     */
    public Color getSelectionColor() {
        return selectionRectangle.getColor();
    }

    /**
     * Get the shadow color.
     * @return Returns the shadow color.
     */
    public Color getShadowColor() {
        return text.getShadowColor();
    }

    /**
     * Set the shadow color.
     * @param shadowColor The shadow color to set.
     */
    public void setShadowColor(Color shadowColor) {
        text.setShadowColor(shadowColor);
    }

    /**
     * Check whether the text has a shadow.
     * @return Returns true if the text has a shadow.
     */
    public boolean isEnableShadow() {
        return this.text.isEnableShadow();
    }
    
    /**
     * Set whether the text has a color.
     * @param enable True to enable the shadow of the text.
     */
    public void setEnableShadow(boolean enable) {
        this.text.setEnableShadow(enable);
    }

    /**
     * Get the font.
     * @return Returns the font.
     */
    public AngelCodeFont getFont() {
        return text.getFont();
    }

    /**
     * Set the font.
     * @param font The font to set.
     */
    public void setFont(AngelCodeFont font) {
        text.setFont(font);
    }
    
    /**
     * Check whether the element is disabled. There can't be written anything in a disabled element.
     * @return Returns true if the element is disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Set whether the element is disabled. There can't be written anything in a disabled element. 
     * @param disabled True to disable the element.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        
        if (disabled) {
            setFocus(null);
            cursor.setVisible(false);
            resetSelection();
            selectionRectangle.setVisible(false);
        }
    }
    
    /**
     * Get the padding of the text from the left and right side.
     * @return Returns the padding.
     */
    public Vector2f getPadding() {
        return padding;
    }

    /**
     * Set the padding of the text from the left and right side.
     * @param padding The padding, where x = left, y = right.
     */
    public void setPadding(Vector2f padding) {
        this.padding = padding;
    }
    
    private void notifySelectionChangedListeners() {
        for (SelectionChangedListener listener : selectionListeners) {
            listener.changed(this);
        }
    }

    public void addSelectionChangedListener(SelectionChangedListener listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionChangedListener(SelectionChangedListener listener) {
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
}
