/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A radial menu widget
 */
public class UIRadialRing extends CoreWidget {

    @LayoutConfig
    private List<UIRadialSection> sections = new ArrayList<>();

    public int getSelectedTab() {
        return selectedTab;
    }

    private int selectedTab = -1;
    private boolean hasInitialised;
    private int radius;
    private double sectionAngle;

    private BaseInteractionListener baseInteractionListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            int sectionOver = getSectionOver(event.getRelativeMousePosition());
            if (selectedTab != -1) {
                sections.get(selectedTab).setSelected(false);
            }

            if (sectionOver != -1) {
                sections.get(sectionOver).setSelected(true);
            }
            selectedTab = sectionOver;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (selectedTab != -1) {
                sections.get(selectedTab).setSelected(false);
                sections.get(selectedTab).activateSection();
                selectedTab = -1;
            }
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasInitialised) {
            hasInitialised = true;
            initialise(canvas);
        }
        canvas.addInteractionRegion(baseInteractionListener);
        for (UIRadialSection section : sections) {
            canvas.drawWidget(section);
        }
    }

    private void initialise(Canvas canvas) {
        final double circleToSquare = 0.707106781;
        Rect2i region = canvas.getRegion();

        int sectionWidth = region.width() / 4;
        double offset = sectionWidth * 1.5;
        radius = sectionWidth * 2;
        sectionAngle = (Math.PI * 2) / sections.size();
        int infoSquareSize = (int) (radius * circleToSquare);
        int sectionSquareSize = (int) (sectionWidth * circleToSquare);
        Rect2i infoRegion = Rect2i.createFromMinAndSize(
                sectionWidth + infoSquareSize / 4,
                sectionWidth + infoSquareSize / 4,
                infoSquareSize,
                infoSquareSize);

        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).setDrawRegion(Rect2i.createFromMinAndSize(
                    (int) (Math.cos(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5),
                    (int) (Math.sin(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5),
                    sectionWidth, sectionWidth));
            sections.get(i).setInnerRegion(Rect2i.createFromMinAndSize(
                    (int) (Math.cos(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5 + sectionSquareSize / 4),
                    (int) (Math.sin(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5 + sectionSquareSize / 4),
                    sectionSquareSize, sectionSquareSize));
            sections.get(i).setInfoRegion(infoRegion);
        }
    }

    /**
     * Subscribes a listener to a section. Will be triggered when the mouse is released over that section
     *
     * @param sectionNum The section to attach the listener to
     * @param listener   The listener to attach
     */
    public void subscribe(int sectionNum, ActivateEventListener listener) {
        if (sectionNum >= 0 && sectionNum < sections.size()) {
            sections.get(sectionNum).addListener(listener);
        }
    }

    public int addSection(UIRadialSection section) {
        sections.add(section);
        hasInitialised = false;
        return sections.size() - 1;
    }

    public UIRadialSection getSection(int index) {
        return sections.get(index);
    }

    public void setSections(List<UIRadialSection> sections) {
        this.sections = sections;
        hasInitialised = false;
    }

    public List<UIRadialSection> getSections() {
        return sections;
    }

    /**
     * Unsubscribes a listener from a section. It will no longer be triggered by that section
     *
     * @param sectionNum The section to unsubscribe from
     * @param listener   The listener to unsubscribe
     */
    public void unsubscribe(int sectionNum, ActivateEventListener listener) {
        if (sectionNum >= 0 && sectionNum < sections.size()) {
            sections.get(sectionNum).removeListener(listener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return canvas.getRegion().size();
    }


    private int getSectionOver(Vector2i mousePos) {
        mousePos.x -= radius;
        mousePos.y -= radius;

        double angle = Math.atan2(mousePos.y(), mousePos.x());
        angle = angle < 0 ? angle + Math.PI * 2 : angle;

        double dist = Math.sqrt((double) mousePos.x() * mousePos.x() + (double) mousePos.y() * mousePos.y());
        if (dist < radius / 2 || dist > radius) {
            return -1;
        }

        return (int) Math.floor(angle / sectionAngle);
    }

}
