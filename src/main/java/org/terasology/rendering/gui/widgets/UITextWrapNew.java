package org.terasology.rendering.gui.widgets;

import java.util.ArrayList;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.events.ChangedListener;

/**
 * A text container supporting automatic text wrapping.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UITextWrapNew extends UIDisplayContainerScrollable {
    
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();

    private StringBuilder originalText;
    private final UIText text;

    public UITextWrapNew(Vector2f size) {
        setSize(size);
        setPadding(new Vector4f(5f, 5f, 5f, 5f));
        setEnableScrolling(true);
        setEnableScrollbar(true);
        
        text = new UIText();
        text.setColor(Color.black);
        text.setVisible(true);
        
        addDisplayElement(text);
    }
    
    /**
     * Wraps the text to the size of the container.
     * @param text The text to wrap.
     * @return Returns the wrapped text.
     */
    private String wrapText(String text) {
        int lastSpace = 0;
        int lastWrap = 0;
        StringBuilder wrapText = new StringBuilder(text + " ");
        
        //loop through whole text
        for (int i = 0; i < wrapText.length(); i++) {
            
            //check if character is a space -> text can only be wrapped at spaces
            if (wrapText.charAt(i) == ' ') {
                //check if the string (from the beginning of the new line) is bigger than the container width
                if (calcTextWidth(wrapText.substring(lastWrap, i)) > getScrollContainerSize().x) {
                    //than wrap the text at the previous space
                    wrapText.insert(lastSpace + 1, '\n');
                    
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
     * Get the displayed text of the label.
     * @return Returns the text.
     */
    public String getText() {
        return originalText.toString();
    }
    
    /**
     * Set the text of the label.
     * @param text The text to set.
     */
    public void setText(String text) {
        boolean scrollbarVisibility = isScrollbarVisible();
        
        this.text.setText(wrapText(text));
        this.originalText = new StringBuilder(text);

        calcContentHeight();
        
        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(originalText.toString());
        }
        
        notifyChangedListeners();
    }
    
    /**
     * Append a text to the current displayed text of the label.
     * @param text The text to append.
     */
    public void appendText(String text) {
        boolean scrollbarVisibility = isScrollbarVisible();
        
        this.text.setText(wrapText(originalText.append(text).toString()));
        
        calcContentHeight();
        
        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(originalText.toString());
        }
        
        notifyChangedListeners();
    }
    
    /**
     * Insert a text at a specific position into the current displayed text of the label.
     * @param offset The offset, where to insert the text at.
     * @param text The text to insert.
     */
    public void insertText(int offset, String text) {
        boolean scrollbarVisibility = isScrollbarVisible();
        
        this.text.setText(wrapText(originalText.insert(offset, text).toString()));
        
        calcContentHeight();
        
        //if the visibility of the scrollbar changed, recalculate the content
        if (scrollbarVisibility != isScrollbarVisible()) {
            setText(text);
        }
        
        notifyChangedListeners();
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
