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

import com.google.gson.annotations.SerializedName;
import org.terasology.rendering.nui.LayoutHint;

/**
 * @author Immortius
 */
public class RelativeLayoutHint implements LayoutHint {

    private int width;
    private int height;

    @SerializedName("position-left")
    private HorizontalInfo positionLeft;
    @SerializedName("position-right")
    private HorizontalInfo positionRight;
    @SerializedName("position-horizontal-center")
    private HorizontalInfo positionCenterHorizontal;

    @SerializedName("position-top")
    private VerticalInfo positionTop;
    @SerializedName("position-bottom")
    private VerticalInfo positionBottom;
    @SerializedName("position-vertical-center")
    private VerticalInfo positionCenterVertical;

    public RelativeLayoutHint() {
    }

    public RelativeLayoutHint(HorizontalHint horizontal, VerticalHint vertical) {
        width = horizontal.getWidth();
        positionLeft = horizontal.getPositionLeft();
        positionCenterHorizontal = horizontal.getPositionCenter();
        positionRight = horizontal.getPositionRight();

        height = vertical.getHeight();
        positionTop = vertical.getPositionTop();
        positionCenterVertical = vertical.getPositionCenter();
        positionBottom = vertical.getPositionBottom();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public HorizontalInfo getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(HorizontalInfo positionLeft) {
        this.positionLeft = positionLeft;
    }

    public HorizontalInfo getPositionRight() {
        return positionRight;
    }

    public void setPositionRight(HorizontalInfo positionRight) {
        this.positionRight = positionRight;
    }

    public HorizontalInfo getPositionCenterHorizontal() {
        return positionCenterHorizontal;
    }

    public void setPositionCenterHorizontal(HorizontalInfo positionCenterHorizontal) {
        this.positionCenterHorizontal = positionCenterHorizontal;
    }

    public VerticalInfo getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(VerticalInfo positionTop) {
        this.positionTop = positionTop;
    }

    public VerticalInfo getPositionBottom() {
        return positionBottom;
    }

    public void setPositionBottom(VerticalInfo positionBottom) {
        this.positionBottom = positionBottom;
    }

    public VerticalInfo getPositionCenterVertical() {
        return positionCenterVertical;
    }

    public void setPositionCenterVertical(VerticalInfo positionCenterVertical) {
        this.positionCenterVertical = positionCenterVertical;
    }

}
