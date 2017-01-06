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
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2i pos = event.getRelativeMousePosition();
            pos = new Vector2i(pos.x() - (int) radius, pos.y() - (int) radius);
            double angle = Math.atan2(pos.y(), pos.x());

            if (angle < 0) {
                angle += Math.PI * 2;
            }

            int newTab = (int) Math.floor((angle * 10) / 8);
            if (selectedTab != newTab) {
                if (selectedTab != -1) {
                    sections[submenuLayer][selectedTab].setSelected(false);
                }
                sections[submenuLayer][newTab].setSelected(true);
                selectedTab = newTab;
            }

            if (Math.sqrt(pos.x() * pos.x() + pos.y() * pos.y()) > radius && selectedTab != -1) {
                sectionSelected();
            }
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (selectedTab != -1) {
                sectionSelected();
                submenuLayer = 0;
            }
        }
    };

    private void sectionSelected() {
        sections[submenuLayer][selectedTab].setSelected(false);
        sections[submenuLayer][selectedTab].activateSection();
        selectedTab = -1;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasInitialised) {
            hasInitialised = true;
            initialise(canvas);
        }

        canvas.addInteractionRegion(baseInteractionListener);

        canvas.drawBackground();
        for (UIRadialSection section : sections[submenuLayer]) {
            if (section != null) {
                canvas.drawWidget(section);
            }
        }
    }

    public void initialise(Canvas canvas) {
        region = canvas.getRegion();
        int ringWidth = (int) (0.2578125 * region.width() * 2);
        int innerRadius = (int) (0.2421875 * region.width() * 2);
        radius = ringWidth + innerRadius;
        logger.info("rad " + radius);

        Rect2i infoRegion = Rect2i.createFromMinAndSize(
                region.width() / 2,
                region.height() / 2,
                innerRadius * 2,
                innerRadius * 2);

        for (int q = 0; q < sections.length; q++) {
            for (int i = 0; i < 8; i++) {
                if (sections[submenuLayer][i] != null) {

                    double offset = innerRadius + ringWidth * q;
                    logger.info(offset + " Offset");
                    sections[submenuLayer][i].setCenter(Rect2i.createFromMinAndSize(
                            (int) (Math.cos(i * Math.PI / 4 + Math.PI / 8) * offset) + region.width() / 2 - ringWidth / 2,
                            (int) (Math.sin(i * Math.PI / 4 + Math.PI / 8) * offset) + region.width() / 2 - ringWidth / 2,
                            ringWidth, ringWidth));
                    sections[submenuLayer][i].setSelectedTexture(Assets.getTextureRegion("engine:RadialSelected" + i).get());
                    sections[submenuLayer][i].setSectionTexture(Assets.getTextureRegion("engine:RadialBase" + i).get());
                    sections[submenuLayer][i].setInfoRegion(infoRegion);
                    /*sections[submenuLayer][i].setDrawRegion(Rect2i.createFromMinAndSize(
                            center.x - edgeWidth / 2 + edgeWidth * 2,
                            center.y - edgeHeight / 2 + edgeHeight * 2,
                            edgeWidth,
                            edgeHeight));*/
                } else {
                    sections[submenuLayer][i] = new UIRadialEmpty();
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

}
