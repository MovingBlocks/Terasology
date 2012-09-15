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

import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayElement.EHorizontalAlign;
import org.terasology.rendering.gui.framework.UIDisplayElement.EVerticalAlign;
import org.terasology.rendering.gui.framework.style.Style;

/**
 * The GridLayout positions display elements within a UIComposite container in a grid depending on the number of columns the grid has.
 * Display elements are laid out in columns from left to right. A new row is created when numColumns + 1 display elements where added.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @see org.eclipse.swt.layout.GridLayout
 */
//TODO support for horizontal/vertical span. Could be kinda tricky.
public class GridLayout implements Layout {
    
    //layout
    private Vector2f size = new Vector2f(0, 0);
    private int columns;
    private Vector4f padding = new Vector4f(0, 0, 0, 0); //top, right, bottom, left
    private float minCellWidth = 0f;
    private float minCellHeight = 0f;
    private float[] cellWidth;
    private float[] cellHeight;
    
    //cell align
    private EVerticalAlign verticalCellAlign = EVerticalAlign.TOP;
    private EHorizontalAlign horizontalCellAlign = EHorizontalAlign.LEFT;
    
    //options
    private boolean enableBorder = false;
    private float borderWidth = 1f;
    private Color borderColor = Color.black;
    private boolean equalWidth = false;

    public GridLayout(int columns) {
        this.columns = columns;
    }

    public GridLayout(int columns, boolean equalWidth) {
        this.columns = columns;
        this.equalWidth = equalWidth;
    }
    
    public GridLayout(int columns, int minCellWidth) {
        this.columns = columns;
        this.minCellWidth = minCellWidth;
    }
    
    @Override
    public void render() {
        if (enableBorder) {
            glLineWidth(borderWidth);
            glBegin(GL11.GL_LINES);
            glColor4f(borderColor.r, borderColor.g, borderColor.b, borderColor.a);
            
            for (int i = 1; i < cellWidth.length; i++) {
                //calculate x position
                float x = 0;
                for (int j = 0; (j < i); j++) {
                    x += cellWidth[j];
                }
                
                glVertex2f(x, 0f);
                glVertex2f(x, size.y);
            }
            
            for (int i = 1; i < cellHeight.length; i++) {
                //calculate y position
                float y = 0;
                for (int j = 0; j < i; j++) {
                    y += cellHeight[j];
                }
    
                glVertex2f(0f, y);
                glVertex2f(size.x, y);
            }
            
            glEnd();
        }
    }
    
    @Override
    public void layout(UIDisplayContainer container, boolean fitSize) {
        List<UIDisplayElement> allElements = container.getDisplayElements();
        List<UIDisplayElement> elements = new ArrayList<UIDisplayElement>();
        
        for (UIDisplayElement element : allElements) {
            if (!(element instanceof Style)) {
                elements.add(element);
            }
        }
        
        cellWidth = calcCellWidth(elements);
        cellHeight = calcCellHeight(elements);
        
        for (int i = 0; i < elements.size(); i++) {
            //calculate x position
            float x = 0;
            for (int j = 0; (j < (i % columns)); j++) {
                x += cellWidth[j % columns];
            }
            
            //horizontal align
            if (horizontalCellAlign == EHorizontalAlign.LEFT) {
                x += padding.w;
            } else if (horizontalCellAlign == EHorizontalAlign.CENTER) {
                x += cellWidth[i % columns] / 2 - elements.get(i).getSize().x / 2;
            } else if (horizontalCellAlign == EHorizontalAlign.RIGHT) {
                x += cellWidth[i % columns] - elements.get(i).getSize().x + padding.y;
            }
            
            //calculate y position
            float y = 0;
            for (int j = 0; j < (int) Math.floor(i / columns); j++) {
                y += cellHeight[(int) Math.floor(j / columns)];
            }
            
            //vertical align
            if (verticalCellAlign == EVerticalAlign.TOP) {
                y += padding.x;
            } else if (verticalCellAlign == EVerticalAlign.CENTER) {
                y += cellHeight[(int) Math.floor(i / columns)] / 2 - elements.get(i).getSize().y / 2;
            } else if (verticalCellAlign == EVerticalAlign.BOTTOM) {
                y += cellHeight[(int) Math.floor(i / columns)] - elements.get(i).getSize().y - padding.z;
            }

            
            elements.get(i).setPosition(new Vector2f(x, y));
            elements.get(i).setVisible(true);                   //TODO remove
        }
        
        if (fitSize) {
            container.setSize(size);
        }
    }
    
    private float[] calcCellWidth(List<UIDisplayElement> elements) {
        float[] width = new float[columns];
        
        //calculate width of each column
        for (int i = 0; i < elements.size(); i++) {
            width[i % columns] = Math.max(width[i % columns], elements.get(i).getSize().x);
        }
        
        //add padding
        for (int i = 0; i < width.length; i++) {
            width[i] += (padding.w + padding.y);
        }
        
        //min cell width
        if (minCellWidth > 0) {
            for (int i = 0; i < width.length; i++) {
                if (width[i] < minCellWidth) {
                    width[i] = minCellWidth;
                }
            }
        }
        
        //if equal width is on
        if (equalWidth) {
            //choose widest column
            float max = width[0];
            for (int i = 0; i < width.length; i++) {
                if (width[i] > max) {
                    max = width[i];
                }
            }
            
            //set all columns to the max width
            for (int i = 0; i < width.length; i++) {
                width[i] = max;
            }
        }
        
        //calculate container width
        size.x = 0f;
        for (int i = 0; i < width.length; i++) {
            size.x += width[i];
        }

        return width;
    }

    private float[] calcCellHeight(List<UIDisplayElement> elements) {
        int rows = (int) Math.max(1f, Math.ceil(((float)elements.size() / (float)columns)));
        float[] height = new float[rows];
 
        //calculate height of each row
        for (int i = 0; i < elements.size(); i++) {
            height[(int) Math.floor(i / columns)] = Math.max(height[(int) Math.floor(i / columns)], elements.get(i).getSize().y);
        }
        
        //add padding
        for (int i = 0; i < height.length; i++) {
            height[i] += (padding.x + padding.z);
        }
        
        //custom cell height
        if (minCellHeight > 0) {
            for (int i = 0; i < height.length; i++) {
                if (height[i] < minCellHeight) {
                    height[i] = minCellHeight;
                }
            }
        }
        
        //calculate container height
        size.y = 0f;
        for (int i = 0; i < height.length; i++) {
            size.y += height[(int) Math.floor(i / columns)];
        }

        return height;
    }
    
    /**
     * Get the vertical align of elements within each cell.
     * @return Returns the align.
     */
    public EVerticalAlign getVerticalCellAlign() {
        return verticalCellAlign;
    }
    
    /**
     * Set the vertical positioning of elements within each cell.
     * @param align The align which can be top, center or bottom.
     */
    public void setVerticalCellAlign(EVerticalAlign align) {
        this.verticalCellAlign = align;
    }

    /**
     * Get the horizontal align of elements within each cell.
     * @return Returns the align.
     */
    public EHorizontalAlign getHorizontalCellAlign() {
        return horizontalCellAlign;
    }

    /**
     * Set the horizontal positioning of elements within each cell.
     * @param align The align which can be left, center or right.
     */
    public void setHorizontalCellAlign(EHorizontalAlign align) {
        this.horizontalCellAlign = align;
    }

    /**
     * Get the padding of each cell.
     * @return Returns the padding.
     */
    public Vector4f getPadding() {
        return padding;
    }

    /**
     * Set the padding of each cell. 
     * @param padding The padding, where x = top, y = right, z = bottom and w = left.
     */
    public void setPadding(Vector4f padding) {
        this.padding = padding;
    }

    /**
     * Get the number of columns of the grid layout.
     * @return Returns the number of columns.
     */
    public int getColumns() {
        return columns;
    }
    
    /**
     * Set the number of columns of the grid layout. Display elements are laid out in these columns from left to right. A new row is created when numColumns + 1 display elements where added.
     * @param columns The number of columns.
     */
    public void setColumns(int columns) {
        this.columns = columns;
    }
    
    /**
     * Get the minimum width of each column.
     * @return Returns the minimum width.
     */
    public float getMinCellWidth() {
        return minCellWidth;
    }

    /**
     * Set the minimum width of each column.
     * @param width The minimum width.
     */
    public void setMinCellWidth(float width) {
        this.minCellWidth = width;
    }

    /**
     * Get the minimum height of each row.
     * @return Returns the minimum height.
     */
    public float getMinCellHeight() {
        return minCellHeight;
    }

    /**
     * Set the minimum height for each row.
     * @param height The minimum height.
     */
    public void setMinCellHeight(float height) {
        this.minCellHeight = height;
    }
    
    /**
     * Check whether each column has the same width.
     * @return Returns true if each row has the same width.
     */
    public boolean isEqualWidth() {
        return equalWidth;
    }

    /**
     * Set whether each column will have the same width. The width of the largest column width will be used.
     * @param equalWidth True to enable equal width.
     */
    public void setEqualWidth(boolean equalWidth) {
        this.equalWidth = equalWidth;
    }
    
    /**
     * Check whether the grid has a border.
     * @return Returns true if the grid has a border.
     */
    public boolean isEnableBorder() {
        return enableBorder;
    }

    /**
     * Set whether the grid will have a border.
     * @param enable True to enable the border.
     */
    public void setEnableBorder(boolean enable) {
        this.enableBorder = enable;
    }

    /**
     * Get the border width for the borders of the grid.
     * @return Returns the width.
     */
    public float getBorderWidth() {
        return borderWidth;
    }

    /**
     * Set the border width for the borders of the grid.
     * @param width The width.
     */
    public void setBorderWidth(float width) {
        this.borderWidth = width;
    }

    /**
     * Get the border color for the borders of the grid.
     * @return Returns the border color.
     */
    public Color getBorderColor() {
        return borderColor;
    }
    
    /**
     * Set the border color for the borders of the grid.
     * @param color The border color.
     */
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }
}