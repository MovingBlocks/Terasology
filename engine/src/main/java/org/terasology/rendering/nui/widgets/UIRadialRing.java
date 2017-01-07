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
import org.terasology.math.geom.Vector2d;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.utilities.Assets;

public class UIRadialRing extends CoreWidget {
    private final Logger logger = LoggerFactory.getLogger(UIRadialRing.class);

    @LayoutConfig
    private UIRadialSection[][] sections = {};

    private Rect2i region;

    private int selectedTab = -1;
    private boolean hasInitialised;
    private double radius;
    private int submenuLayer;

    private BaseInteractionListener baseInteractionListener = new BaseInteractionListener() {

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            if (selectedTab != -1) {
                sections[submenuLayer][selectedTab].setSelected(false);
            }
            selectedTab = getSectionOver(event.getRelativeMousePosition());
            sections[submenuLayer][selectedTab].setSelected(true);
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
                sectionSelected();
                if (sections[submenuLayer][selectedTab].getIsSubmenu()) {
                    submenuLayer = sections[submenuLayer][selectedTab].getSubmenu();
                } else {
                    selectedTab = -1;
                }
            }
        }
    };

    private void sectionSelected() {
        sections[submenuLayer][selectedTab].setSelected(false);
        sections[submenuLayer][selectedTab].activateSection();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasInitialised) {
            hasInitialised = true;
            initialise(canvas);
        }

        canvas.addInteractionRegion(baseInteractionListener);
        canvas.drawTexture(Assets.getTextureRegion("engine:flag_en").get());
        for (UIRadialSection section : sections[submenuLayer]) {
            if (section != null) {
                canvas.drawWidget(section);
            }
        }
    }

    public void initialise(Canvas canvas) {
        region = canvas.getRegion();

        int ringWidth = (int) (0.2578125 * region.width());
        int innerRadius = (int) (0.2421875 * region.width());
        double offset = innerRadius + ringWidth / 2;
        radius = ringWidth + innerRadius;

        Rect2i infoRegion = Rect2i.createFromMinAndSize(
                region.width() / 2,
                region.height() / 2,
                innerRadius * 2,
                innerRadius * 2);

        for (int q = 0; q < sections.length; q++) {
            for (int i = 0; i < 8; i++) {
                if (sections[q][i] != null) {
                    sections[q][i].setCenter(Rect2i.createFromMinAndSize(
                            (int) (Math.cos(i * Math.PI / 4 + Math.PI / 8) * offset),
                            (int) (Math.sin(i * Math.PI / 4 + Math.PI / 8) * offset),
                            region.width() / 2, region.height() / 2));
                    sections[q][i].setSelectedTexture(Assets.getTextureRegion("engine:RadialSelected" + i).get());
                    sections[q][i].setSectionTexture(Assets.getTextureRegion("engine:RadialBase" + i).get());
                    sections[q][i].setInfoRegion(infoRegion);
                    /*sections[submenuLayer][i].setDrawRegion(Rect2i.createFromMinAndSize(
                            center.x - edgeWidth / 2 + edgeWidth * 2,
                            center.y - edgeHeight / 2 + edgeHeight * 2,
                            edgeWidth,
                            edgeHeight));*/
                } else {
                    sections[q][i] = new UIRadialEmpty();
                }
            }
        }
    }

    public void subscribe(int menuNum, int sectionNum, ActivateEventListener listener) {
        sections[menuNum][sectionNum].addListener(listener);
    }

    public void unsubscribe(int menuNum, int sectionNum, ActivateEventListener listener) {
        sections[menuNum][sectionNum].removeListener(listener);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return region.size();
    }

    private int getSectionOver(Vector2i mousePos) {
        logger.info(mousePos.toString());
        mousePos.x -= radius;
        mousePos.y -= radius;

        logger.info(mousePos.toString());
        double angle = Math.atan2(mousePos.y(), mousePos.y());        logger.info(angle + " ang");

        angle += angle < 0 ? Math.PI * 2 : 0;

        return (int) Math.floor(angle * 1.5);

    }

}
