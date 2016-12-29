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

import jdk.nashorn.internal.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.InputSystem;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.utilities.Assets;


public class UIRadialRing extends CoreWidget {
    private final Logger logger = LoggerFactory.getLogger(UIRadialRing.class);

    private Rect2i region;

    private TextureRegion[] selectTextures = new TextureRegion[8];

    private int selectedTab = 1;

    private BaseInteractionListener listener = new BaseInteractionListener() {
        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2i pos = event.getRelativeMousePosition();
            pos = new Vector2i(pos.x() - region.width() / 2, pos.y() - region.height() / 2);
            logger.info("pos: " + pos.toString());
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
            selectedTab = -1;
        }
    };


    private boolean hasInitialised;

    @Override
    public void update(float delta) {
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasInitialised) {
            for (int i = 0; i < 8; i++) {
                selectTextures[i] = Assets.getTextureRegion("engine:tabSelected" + i).get();
            }
            hasInitialised = true;
            region = canvas.getRegion();
        }
        canvas.addInteractionRegion(listener);
        canvas.drawBackground();
        if (selectedTab != -1) {
            canvas.drawTexture(selectTextures[selectedTab]);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return Vector2i.zero();
    }
}
