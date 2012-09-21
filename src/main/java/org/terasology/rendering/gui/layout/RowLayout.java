/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.layout;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.style.Style;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * TODO implement wrap
 */
public class RowLayout implements Layout {
    
    //options
    private float spacingHorizontal = 0f;
    private float spacingVertical = 0f;
    private boolean wrap = false;
    private boolean equalWidth = false;
    
    @Override
    public void layout(UIDisplayContainer container, boolean fitSize) {
        List<UIDisplayElement> allElements = container.getDisplayElements();
        List<UIDisplayElement> elements = new ArrayList<UIDisplayElement>();
        
        for (UIDisplayElement element : allElements) {
            if (element.isVisible() && !(element instanceof Style)) {
                elements.add(element);
            }
        }
        
        Vector2f[] cellSize = calcCellSize(elements);
        if (wrap) {
            if (container.getSize().x > 0) {
                int lastWrap = 0;
                List<Float> rowSizeY = new ArrayList<Float>();
                for (int i = 0; i < cellSize.length; i++) {
                    //calculate x position
                    float x = 0f;
                    for (int j = lastWrap; j < i; j++) {
                        x += cellSize[j].x;
                    }
                    
                    //calculate y position
                    float y = 0f;
                    for (int j = 0; j < rowSizeY.size(); j++) {
                        y += rowSizeY.get(j);
                    }
                    
                    elements.get(i).setPosition(new Vector2f(x, y));
                    
                    //check if content width is to big
                    if ((x + cellSize[i].x) > container.getSize().x) {
                        float max = 0f;
                        for (int j = lastWrap; j < i; j++) {
                            max = Math.max(max, cellSize[j].y);
                        }
                        
                        rowSizeY.add(max);
                        
                        lastWrap = i;
                        
                        //this will recalculate the position of the last element again
                        i--;
                    }
                }
                
                /*
                float max = 0f;
                for (int i = lastWrap; i < cellSize.length; i++) {
                    max = Math.max(max, cellSize[i].y);
                }
                
                rowSizeY.add(max);
                
                String;
                for (Float y : rowSizeY) {
                    
                }
                */
            }
        } else {
            for (int i = 0; i < cellSize.length; i++) {
                //calculate x position
                float x = 0f;
                for (int j = 0; j < i; j++) {
                    x += cellSize[j].x;
                }
                
                elements.get(i).setPosition(new Vector2f(x, 0f));
            }
            
            if (fitSize) {
                Vector2f size = new Vector2f(0f, 0f);
                for (int i = 0; i < cellSize.length; i++) {
                    size.x += cellSize[i].x;
                    size.y = Math.max(size.y, cellSize[i].y);
                }
                
                container.setSize(size);
            }
        }
    }
    
    @Override
    public void render() {
        
    }
    
    private Vector2f[] calcCellSize(List<UIDisplayElement> elements) {
        Vector2f[] cellSize = new Vector2f[elements.size()];
        
        for (int i = 0; i < cellSize.length; i++) {
            cellSize[i] = new Vector2f(0f, 0f);
        }
        
        //get width and height of each cell
        for (int i = 0; i < cellSize.length; i++) {
            cellSize[i].x = elements.get(i).getSize().x + spacingHorizontal;
            cellSize[i].y = elements.get(i).getSize().y + spacingVertical;
        }
        
        //if equal width is on
        if (equalWidth) {
            //choose widest column
            float max = cellSize[0].x;
            for (int i = 0; i < cellSize.length; i++) {
                if (cellSize[i].x > max) {
                    max = cellSize[i].x;
                }
            }
            
            //set all columns to the max width
            for (int i = 0; i < cellSize.length; i++) {
                cellSize[i].x = max;
            }
        }
                
        return cellSize;
    }
    
    /**
     * Get whether the display elements will be wrapped at the width of the container. ! NOT IMPLEMENTED YET !
     * @return
     */
    public boolean isWrap() {
        return wrap;
    }
    
    /**
     * Set whether the display elements will be wrapped at the width of the container.
     * @param wrap True to wrap the display elements.
     */
    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }
    
    /**
     * 
     * @return
     */
    public boolean isEqualWidth() {
        return equalWidth;
    }
    
    /**
     * 
     * @param equalWidth
     */
    public void setEqualWidth(boolean equalWidth) {
        this.equalWidth = equalWidth;
    }
    
    /**
     * 
     * @return
     */
    public float getSpacingHorizontal() {
        return spacingHorizontal;
    }
    
    /**
     * 
     * @param spacingHorizontal
     */
    public void setSpacingHorizontal(float spacingHorizontal) {
        this.spacingHorizontal = spacingHorizontal;
    }
    
    /**
     * 
     * @return
     */
    public float getSpacingVertical() {
        return spacingVertical;
    }
    
    /**
     * 
     * @param spacingVertical
     */
    public void setSpacingVertical(float spacingVertical) {
        this.spacingVertical = spacingVertical;
    }
}
