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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UpdateListener;

import java.util.List;

/**
 * A generic context menu, implemented as a {@link CoreScreenLayer} spanning the canvas area it should be created within.
 */
public class ContextMenuScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:contextMenuScreen");

    /**
     *
     */
    private List<ContextMenuLevel> menuLevels = Lists.newArrayList();
    /**
     * Listeners fired when the menu is closed (without selecting an option).
     */
    private List<UpdateListener> closeListeners = Lists.newArrayList();
    /**
     *
     */
    private List<UpdateListener> screenClosedListeners = Lists.newArrayList();

    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            // Close the context menu on click outside it.
            closeListeners.forEach(UpdateListener::onAction);
            getManager().closeScreen(ASSET_URI);

            return false;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            // Close the context menu on mouse wheel scroll outside it.
            closeListeners.forEach(UpdateListener::onAction);
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
    public void onClosed() {
        screenClosedListeners.forEach(UpdateListener::onAction);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(mainListener);

        Vector2i currentPosition = null;
        int currentWidth = 0;
        for (ContextMenuLevel level : menuLevels) {
            if (level.isVisible()) {
                if (currentPosition == null) {
                    currentPosition = new Vector2i(level.getPosition());
                } else {
                    currentPosition.addX(currentWidth);
                }
                UIWidget menuWidget = level.getMenuWidget();
                Rect2i region = Rect2i.createFromMinAndSize(currentPosition,
                    canvas.calculatePreferredSize(menuWidget));
                currentWidth = canvas.calculatePreferredSize(menuWidget).getX() - 8;
                canvas.drawWidget(level.getMenuWidget(), region);
            }
        }
    }

    public void setMenuLevels(List<ContextMenuLevel> levels) {
        menuLevels = levels;
    }

    public void subscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.add(listener);
    }

    public void unsubscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.remove(listener);
    }

    public void subscribeScreenClosed(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        screenClosedListeners.add(listener);
    }

    public void unsubscribeScreenClosed(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        screenClosedListeners.remove(listener);
    }
}
