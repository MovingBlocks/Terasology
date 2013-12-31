/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.layout;

import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ContainerWrapper;
import org.terasology.rendering.nui.UIWidget;

/**
 * @author synopia
 */
public class MigComponent implements ComponentWrapper {
    private int x;
    private int y;
    private int width;
    private int height;
    private int screenLocationX;
    private int screenLocationY;
    private int minimumWidth;
    private int minimumHeight;
    private int preferredWidth = 100;
    private int preferredHeight = 20;

    private UIWidget widget;
    private MigLayout parent;

    public MigComponent(MigLayout parent, UIWidget widget) {
        this.widget = widget;
        this.parent = parent;
    }

    @Override
    public UIWidget getComponent() {
        return widget;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getScreenLocationX() {
        return screenLocationX;
    }

    @Override
    public int getScreenLocationY() {
        return screenLocationY;
    }

    @Override
    public int getMinimumWidth(int hHint) {
        return minimumWidth;
    }

    @Override
    public int getMinimumHeight(int wHint) {
        return minimumHeight;
    }

    @Override
    public int getPreferredWidth(int hHint) {
        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int wHint) {
        return preferredHeight;
    }

    @Override
    public int getMaximumWidth(int hHint) {
        return Short.MAX_VALUE;
    }

    @Override
    public int getMaximumHeight(int wHint) {
        return Short.MAX_VALUE;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public int getBaseline(int width, int height) {
        return -1;
    }

    @Override
    public boolean hasBaseline() {
        return false;
    }

    @Override
    public ContainerWrapper getParent() {
        return parent;
    }

    @Override
    public float getPixelUnitFactor(boolean isHor) {
        return 1;
    }

    @Override
    public int getHorizontalScreenDPI() {
        return 72;
    }

    @Override
    public int getVerticalScreenDPI() {
        return 72;
    }

    @Override
    public int getScreenWidth() {
        return 800;
    }

    @Override
    public int getScreenHeight() {
        return 600;
    }

    @Override
    public String getLinkId() {
        return null;
    }

    public void setParent(MigLayout parent) {
        this.parent = parent;
    }

    @Override
    public int getLayoutHashCode() {
        int h = 43;
        h += (width) + (height << 5);
        h += (preferredWidth << 10) + (preferredHeight << 15);

        return h;
    }

    @Override
    public int[] getVisualPadding() {
        return null;
    }

    @Override
    public void paintDebugOutline() {

    }

    @Override
    public int getComponetType(boolean disregardScrollPane) {
        return TYPE_UNKNOWN;
    }

    @Override
    public final int hashCode() {
        return widget.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MigComponent)) {
            return false;
        }
        return widget.equals(((MigComponent) obj).getComponent());
    }
}
