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
package org.terasology.rendering.nui.contextMenu;

import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.List;

/**
 * A generic context menu, implemented as a {@link CoreScreenLayer} spanning the canvas area it should be created within.
 */
public class ContextMenuScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:contextMenuScreen");

    /**
     * A list of widgets to be used to draw the context menu.
     */
    private List<UIList<AbstractContextMenuItem>> menuWidgets = Lists.newArrayList();
    /**
     * The initial position of the menu.
     */
    private Vector2i position;

    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            // Close the context menu on click outside it.
            getManager().closeScreen(ASSET_URI);

            return false;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            // Close the context menu on mouse wheel scroll outside it.
            getManager().closeScreen(ASSET_URI);

            // Consume the event to prevent awkward rendering if the menu is within a scrollable widget.
            return true;
        }
    };

    @Override
    public void initialise() {
        find("menu", UIList.class).setCanBeFocus(false);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(mainListener);
        Vector2i currentPosition = null;
        int currentWidth = 0;
        for (UIList<AbstractContextMenuItem> level : menuWidgets) {
            if (level.isVisible()) {
                if (currentPosition == null) {
                    currentPosition = new Vector2i(position);
                } else {
                    currentPosition.addX(currentWidth);
                }
                Rect2i region = Rect2i.createFromMinAndSize(currentPosition,
                    canvas.calculatePreferredSize(level));
                double percentageThreshold = 0.9;
                if (region.maxY() > canvas.getRegion().height() * percentageThreshold) {
                    region = Rect2i.createFromMinAndMax(region.minX(),
                        region.minY()
                                - (region.maxY()
                                - canvas.getRegion().height())
                                - (int) (canvas.getRegion().height() * (1 - percentageThreshold)),
                        region.maxX(),
                        canvas.getRegion().height());
                }
                currentWidth = canvas.calculatePreferredSize(level).getX() - 8;
                canvas.drawWidget(level, region);
            }
        }
    }

    public void setMenuWidgets(List<UIList<AbstractContextMenuItem>> levels) {
        menuWidgets = levels;
    }

    public void setPosition(Vector2i position) {
        this.position = position;
    }
}
