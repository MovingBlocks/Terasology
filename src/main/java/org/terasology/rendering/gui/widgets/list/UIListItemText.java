package org.terasology.rendering.gui.widgets.list;

import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.widgets.UILabel;

/**
 * A list item.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIListItemText extends UIListItem {
    
    private final UILabel label;
    private Color color = Color.white;

    public UIListItemText(String text, Object value) {
        setValue(value);
        
        //TODO remove this once styling system is in place
        addMouseMoveListener(new MouseMoveListener() {        
            @Override
            public void leave(UIDisplayElement element) {
                if(!isSelected()) {
                    label.setColor(color);
                }
            }
            
            @Override
            public void hover(UIDisplayElement element) {

            }
            
            @Override
            public void enter(UIDisplayElement element) {
                if(!isSelected() && !isDisabled()) {
                    label.setColor(Color.orange);
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
                setSize("100%", label.getSize().y + "px");
            }
        });
        label.setText(text);
        label.setVisible(true);

        addDisplayElement(label);
    }
    
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        
        if (!selected) {
            label.setColor(color);
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        label.setColor(color);
    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setPadding(Vector4f padding) {
        label.setMargin(padding);
    }
    
    public Vector4f getPadding() {
        return label.getMargin();
    }
}
