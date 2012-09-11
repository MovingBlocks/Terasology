package org.terasology.rendering.gui.widgets;

import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.style.UIStyle;

/**
 * A list item. As default the list item contains a UIlabel to display a text.
 * Fancy list items can be achieved by adding child elements to the list item.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIListItem extends UIDisplayContainer {
    
    private Object value;
    private boolean isDisabled = false;
    private boolean isSelected = false;
    
    //child elements
    private UILabel label;
    
    //options
    private Color textColor = Color.white;
    private Color textSelectionColor = Color.orange;
    private Color selectionColor = new Color(0xE1 / 255f, 0xDD / 255f, 0xD4 / 255f, 1.0f);
    
    public UIListItem(Object value) {
        setup("", value);
    }

    public UIListItem(String text, Object value) {
        setup(text, value);
    }
    
    private void setup(String text, Object value) {
        setValue(value);
        
        //TODO remove this once styling system is in place
        addMouseMoveListener(new MouseMoveListener() {        
            @Override
            public void leave(UIDisplayElement element) {
                if(!isSelected()) {
                    label.setColor(textColor);
                }
            }
            
            @Override
            public void hover(UIDisplayElement element) {
                
            }
            
            @Override
            public void enter(UIDisplayElement element) {
                if(!isSelected() && !isDisabled()) {
                    label.setColor(textSelectionColor);
                }
            }

            @Override
            public void move(UIDisplayElement element) {
                
            }
        });
        
        label = new UILabel();
        label.setWrap(true);
        label.setSize("100%", "0px");
        label.setVerticalAlign(EVerticalAlign.CENTER);
        label.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                calcHeight();
            }
        });
        setText(text);
        
        addDisplayElement(label);
    }
    
    private void calcHeight() {
        //loop through all display elements
        float max = 0;
        float maxElement = 0;
        for (UIDisplayElement element : getDisplayElements()) {
            if (element instanceof UIStyle || !element.isVisible()) {
                continue;
            }
            
            maxElement = element.getPosition().y + element.getSize().y;
            if (maxElement > max) {
                max = maxElement;
            }
        }
        
        setSize("100%", max + "px");
    }
    
    @Override
    public void addDisplayElement(UIDisplayElement element) {
        super.addDisplayElement(element);
        calcHeight();
    }
    
    @Override
    public void addDisplayElementToPosition(int position, UIDisplayElement element) {
        super.addDisplayElementToPosition(position, element);
        calcHeight();
    }
    
    @Override
    public void removeDisplayElement(UIDisplayElement element) {
        super.removeDisplayElement(element);
        calcHeight();
    }
    
    @Override
    public void removeAllDisplayElements() {
        super.removeAllDisplayElements();
        calcHeight();
    }
        
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }
        
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        
        if (isSelected) {
            setBackgroundColor((int)(selectionColor.r * 255), (int)(selectionColor.g * 255), (int)(selectionColor.b * 255), selectionColor.a);
            label.setColor(textSelectionColor);
        } else {
            label.setColor(textColor);
            removeBackgroundColor();
        }
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color color) {
        textColor = color;
        label.setColor(color);
    }

    public Color getTextSelectionColor() {
        return textSelectionColor;
    }

    public void setTextSelectionColor(Color color) {
        textSelectionColor = color;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color color) {
        selectionColor = color;
    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        if (!text.isEmpty()) {
            label.setText(text);
            label.setVisible(true);
        } else {
            label.setVisible(false);
        }
    }

    //TODO padding should affect all child elements
    public void setPadding(Vector4f padding) {
        label.setMargin(padding);
    }
    
    public Vector4f getPadding() {
        return label.getMargin();
    }
}
