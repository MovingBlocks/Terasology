package org.terasology.rendering.gui.widgets.list;

import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * A list item.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public abstract class UIListItem extends UIDisplayContainer {
    
    private Object value;
    private boolean isDisabled = false;
    private boolean isSelected = false;
    
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
            setBackgroundColor(0xE1, 0xDD, 0xD4, 1.0f);
        } else {
            removeBackgroundColor();
        }
    }
}
