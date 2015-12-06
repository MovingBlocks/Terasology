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
package org.terasology.rendering.nui.layouts.relative;

import org.terasology.rendering.nui.VerticalAlign;

/**
 */
public class VerticalHint {
    private int height;

    private VerticalInfo positionTop;
    private VerticalInfo positionBottom;
    private VerticalInfo positionCenter;

    public static VerticalHint create() {
        return new VerticalHint();
    }

    public VerticalHint fixedHeight(int value) {
        this.height = value;
        return this;
    }

    public VerticalHint center() {
        return centerRelativeTo("", VerticalAlign.MIDDLE, 0);
    }

    public VerticalHint center(int offset) {
        return centerRelativeTo("", VerticalAlign.MIDDLE, offset);
    }

    public VerticalHint center(VerticalAlign targetPart) {
        return centerRelativeTo("", targetPart, 0);
    }

    public VerticalHint center(VerticalAlign targetPart, int offset) {
        return centerRelativeTo("", targetPart, offset);
    }

    public VerticalHint centerRelativeTo(String widgetId, VerticalAlign targetPart) {
        return centerRelativeTo(widgetId, targetPart, 0);
    }

    public VerticalHint centerRelativeTo(String widgetId, VerticalAlign targetPart, int offset) {
        positionCenter = new VerticalInfo();
        positionCenter.setTarget(targetPart);
        positionCenter.setWidget(widgetId);
        positionCenter.setOffset(offset);
        return this;
    }

    public VerticalHint alignTop() {
        return alignTopRelativeTo("", VerticalAlign.TOP, 0);
    }

    public VerticalHint alignTop(int offset) {
        return alignTopRelativeTo("", VerticalAlign.TOP, offset);
    }

    public VerticalHint alignTop(VerticalAlign targetPart) {
        return alignTopRelativeTo("", targetPart, 0);
    }

    public VerticalHint alignTop(VerticalAlign targetPart, int offset) {
        return alignTopRelativeTo("", targetPart, offset);
    }

    public VerticalHint alignTopRelativeTo(String widgetId, VerticalAlign targetPart) {
        return alignTopRelativeTo(widgetId, targetPart, 0);
    }

    public VerticalHint alignTopRelativeTo(String widgetId, VerticalAlign targetPart, int offset) {
        positionTop = new VerticalInfo();
        positionTop.setTarget(targetPart);
        positionTop.setWidget(widgetId);
        positionTop.setOffset(offset);
        return this;
    }

    public VerticalHint alignBottom() {
        return alignBottomRelativeTo("", VerticalAlign.BOTTOM, 0);
    }

    public VerticalHint alignBottom(int offset) {
        return alignBottomRelativeTo("", VerticalAlign.BOTTOM, offset);
    }

    public VerticalHint alignBottom(VerticalAlign targetPart) {
        return alignBottomRelativeTo("", targetPart, 0);
    }

    public VerticalHint alignBottom(VerticalAlign targetPart, int offset) {
        return alignBottomRelativeTo("", targetPart, offset);
    }

    public VerticalHint alignBottomRelativeTo(String widgetId, VerticalAlign targetPart) {
        return alignBottomRelativeTo(widgetId, targetPart, 0);
    }

    public VerticalHint alignBottomRelativeTo(String widgetId, VerticalAlign targetPart, int offset) {
        positionBottom = new VerticalInfo();
        positionBottom.setTarget(targetPart);
        positionBottom.setWidget(widgetId);
        positionBottom.setOffset(offset);
        return this;
    }

    public int getHeight() {
        return height;
    }

    public VerticalInfo getPositionTop() {
        return positionTop;
    }

    public VerticalInfo getPositionBottom() {
        return positionBottom;
    }

    public VerticalInfo getPositionCenter() {
        return positionCenter;
    }
}
