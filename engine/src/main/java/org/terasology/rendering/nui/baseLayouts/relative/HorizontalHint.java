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
package org.terasology.rendering.nui.baseLayouts.relative;

import org.terasology.rendering.nui.HorizontalAlign;

/**
 * @author Immortius
 */
public class HorizontalHint {
    private int width;

    private HorizontalInfo positionLeft;
    private HorizontalInfo positionRight;
    private HorizontalInfo positionCenter;

    public static HorizontalHint create() {
        return new HorizontalHint();
    }

    public HorizontalHint fixedWidth(int value) {
        this.width = value;
        return this;
    }

    public HorizontalHint center() {
        return centerRelativeTo("", HorizontalAlign.CENTER, 0);
    }

    public HorizontalHint center(int offset) {
        return centerRelativeTo("", HorizontalAlign.CENTER, offset);
    }

    public HorizontalHint center(HorizontalAlign targetPart) {
        return centerRelativeTo("", targetPart, 0);
    }

    public HorizontalHint center(HorizontalAlign targetPart, int offset) {
        return centerRelativeTo("", targetPart, offset);
    }

    public HorizontalHint centerRelativeTo(String widgetId, HorizontalAlign targetPart) {
        return centerRelativeTo(widgetId, targetPart, 0);
    }

    public HorizontalHint centerRelativeTo(String widgetId, HorizontalAlign targetPart, int offset) {
        positionCenter = new HorizontalInfo();
        positionCenter.setTarget(targetPart);
        positionCenter.setWidget(widgetId);
        positionCenter.setOffset(offset);
        return this;
    }

    public HorizontalHint alignLeft() {
        return alignLeftRelativeTo("", HorizontalAlign.LEFT, 0);
    }

    public HorizontalHint alignLeft(int offset) {
        return alignLeftRelativeTo("", HorizontalAlign.LEFT, offset);
    }

    public HorizontalHint alignLeft(HorizontalAlign targetPart) {
        return alignLeftRelativeTo("", targetPart, 0);
    }

    public HorizontalHint alignLeft(HorizontalAlign targetPart, int offset) {
        return alignLeftRelativeTo("", targetPart, offset);
    }

    public HorizontalHint alignLeftRelativeTo(String widgetId, HorizontalAlign targetPart) {
        return alignLeftRelativeTo(widgetId, targetPart, 0);
    }

    public HorizontalHint alignLeftRelativeTo(String widgetId, HorizontalAlign targetPart, int offset) {
        positionLeft = new HorizontalInfo();
        positionLeft.setTarget(targetPart);
        positionLeft.setWidget(widgetId);
        positionLeft.setOffset(offset);
        return this;
    }

    public HorizontalHint alignRight() {
        return alignRightRelativeTo("", HorizontalAlign.RIGHT, 0);
    }

    public HorizontalHint alignRight(int offset) {
        return alignRightRelativeTo("", HorizontalAlign.RIGHT, offset);
    }

    public HorizontalHint alignRight(HorizontalAlign targetPart) {
        return alignRightRelativeTo("", targetPart, 0);
    }

    public HorizontalHint alignRight(HorizontalAlign targetPart, int offset) {
        return alignRightRelativeTo("", targetPart, offset);
    }

    public HorizontalHint alignRightRelativeTo(String widgetId, HorizontalAlign targetPart) {
        return alignRightRelativeTo(widgetId, targetPart, 0);
    }

    public HorizontalHint alignRightRelativeTo(String widgetId, HorizontalAlign targetPart, int offset) {
        positionRight = new HorizontalInfo();
        positionRight.setTarget(targetPart);
        positionRight.setWidget(widgetId);
        positionRight.setOffset(offset);
        return this;
    }

    public int getWidth() {
        return width;
    }

    public HorizontalInfo getPositionLeft() {
        return positionLeft;
    }

    public HorizontalInfo getPositionRight() {
        return positionRight;
    }

    public HorizontalInfo getPositionCenter() {
        return positionCenter;
    }
}
