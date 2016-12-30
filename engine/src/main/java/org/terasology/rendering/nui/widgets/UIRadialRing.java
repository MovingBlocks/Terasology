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
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.List;


public class UIRadialRing extends CoreWidget {
    private final Logger logger = LoggerFactory.getLogger(UIRadialRing.class);

    @LayoutConfig
    private List<UIWidget> preview = new ArrayList<>();
    @LayoutConfig
    private List<UIWidget> icons = new ArrayList<>();

    private Rect2i region;
    private Rect2i previewRegion;
    private Rect2i[] sectionRegion = new Rect2i[8];

    private TextureRegion[] selectTextures = new TextureRegion[8];

    private int selectedTab = -1;
    private boolean hasInitialised;

    private List[] listeners = {new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList()};
    private BaseInteractionListener baseInteractionListener = new BaseInteractionListener() {

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2i pos = event.getRelativeMousePosition();
            pos = new Vector2i(pos.x() - region.width() / 2, pos.y() - region.height() / 2);
            double angle = Math.atan2(pos.y(), pos.x());
            if (angle < 0) {
                angle += Math.PI * 2;
            }
            angle = (angle * 10) / 8;
            selectedTab = (int) Math.floor(angle);
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            activateListeners(selectedTab);
            selectedTab = -1;
        }
    };


    @Override
    public void onDraw(Canvas canvas) {
        if (!hasInitialised) {
            hasInitialised = true;
            initialise(canvas);
        }

        canvas.addInteractionRegion(baseInteractionListener);
        canvas.drawBackground();
        if (selectedTab != -1) {
            canvas.drawTexture(selectTextures[selectedTab]);
            if (preview.get(selectedTab) != null) {
                canvas.drawWidget(preview.get(selectedTab), previewRegion);
            }
        }
        for (int i = 0; i < icons.size(); i++) {
            canvas.drawWidget(icons.get(i), sectionRegion[i]);
        }
    }

    public void initialise(Canvas canvas) {
        logger.info("widget init");
        for (int i = 0; i < 8; i++) {
            selectTextures[i] = Assets.getTextureRegion("engine:tabSelected" + i).get();
        }

        region = canvas.getRegion();
        previewRegion = Rect2i.createFromMinAndSize(
                region.minX() + region.width() / 3,
                region.minY() + region.height() / 3,
                region.width() / 3,
                region.height() / 3);
        int edgeWidth = region.width() / 4;
        int edgeHeight = region.height() / 4;
        double dist = (edgeWidth / 2) * 3;
        logger.info(dist + "");
        for (int i = 0; i < 8; i++) {
            Vector2i center = new Vector2i((int) (Math.cos(i * 0.7853981625 + 0.39269908125) * dist), (int) (Math.sin(i * 0.7853981625 + 0.39269908125) * dist));
            logger.info(center.toString());
            sectionRegion[i] = Rect2i.createFromMinAndSize(center.x - edgeWidth / 2 + edgeWidth * 2, center.y - edgeHeight / 2 + edgeHeight * 2, edgeWidth, edgeHeight);
        }
    }

    public void subscribe(int sectionNum, ActivateEventListener listener) {
        logger.info("subscribe");
        if (sectionNum >= 0 && sectionNum <= 7) {
            listeners[sectionNum].add(listener);
        }
    }

    public void unsubscribe(int sectionNum, ActivateEventListener listener) {
        if (sectionNum >= 0 && sectionNum <= 7) {
            listeners[sectionNum].remove(listener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return Vector2i.zero();
    }

    private void activateListeners(int index) {
        for (Object listener : listeners[index]) {
            ((ActivateEventListener) listener).onActivated(this);
        }
    }
}
