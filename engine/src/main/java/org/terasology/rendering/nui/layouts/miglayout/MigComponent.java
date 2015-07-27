/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layouts.miglayout;

import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ContainerWrapper;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

/**
 * Created by synopia on 06.01.14.
 */
public class MigComponent implements ComponentWrapper {
    private Vector2i pos = new Vector2i();
    private Vector2i size = new Vector2i();
    private Vector2i screenPos = new Vector2i();
    private Vector2i minimumSize = new Vector2i();
    private Vector2i preferredSize = new Vector2i();

    private UIWidget widget;
    private MigLayout parent;

    public MigComponent(MigLayout parent, UIWidget widget) {
        this.widget = widget;
        this.parent = parent;
    }


    public void calculatePreferredSize(Canvas canvas, Vector2i sizeHint) {
        minimumSize = canvas.calculatePreferredSize(widget);
        minimumSize.x = Math.min(sizeHint.x, minimumSize.x);
        minimumSize.y = Math.min(sizeHint.y, minimumSize.y);
        preferredSize = new Vector2i(Math.max(sizeHint.x, minimumSize.x), Math.max(sizeHint.y, minimumSize.y));
    }

    @Override
    public UIWidget getComponent() {
        return widget;
    }

    @Override
    public int getX() {
        return pos.x;
    }

    @Override
    public int getY() {
        return pos.y;
    }

    @Override
    public int getWidth() {
        return size.x;
    }

    @Override
    public int getHeight() {
        return size.y;
    }

    @Override
    public int getScreenLocationX() {
        return screenPos.x;
    }

    @Override
    public int getScreenLocationY() {
        return screenPos.y;
    }

    @Override
    public int getMinimumWidth(int hHint) {
        return minimumSize.x;
    }

    @Override
    public int getMinimumHeight(int wHint) {
        return minimumSize.y;
    }

    @Override
    public int getPreferredWidth(int hHint) {
        return preferredSize.x;
    }

    @Override
    public int getPreferredHeight(int wHint) {
        return preferredSize.y;
    }

    @Override
    public int getMaximumWidth(int hHint) {
        return Short.MAX_VALUE;
    }

    @Override
    public int getMaximumHeight(int wHint) {
        return Short.MAX_VALUE;
    }

    public void setPreferredSize(Vector2i preferredSize) {
        this.preferredSize = preferredSize;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        pos.x = x;
        pos.y = y;
        size.x = width;
        size.y = height;
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
        throw new IllegalAccessError("Not supported!");
    }

    @Override
    public int getScreenHeight() {
        throw new IllegalAccessError("Not supported!");
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
        h += (size.x) + (size.y << 5);
        h += (preferredSize.x << 10) + (preferredSize.y << 15);

        return h;
    }

    @Override
    public int[] getVisualPadding() {
        return null;
    }

    @Override
    public void paintDebugOutline(boolean showVisualPadding) {

    }

    @Override
    public int getComponentType(boolean disregardScrollPane) {
        return TYPE_UNKNOWN;
    }

    @Override
    public int getContentBias() {
        return -1;
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
