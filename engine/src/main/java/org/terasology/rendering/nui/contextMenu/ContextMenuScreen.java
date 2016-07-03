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

import com.google.api.client.util.Lists;
import com.google.common.base.Preconditions;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
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
     * The list containing the available context menu options.
     */
    private UIList menu;
    /**
     * The absolute mouse position of {@code menu}.
     */
    private Vector2i menuPosition = Vector2i.zero();
    /**
     * Listeners triggered when a context menu option is selected.
     */
    private List<UpdateListener> selectionListeners = Lists.newArrayList();

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
        menu = find("menu", UIList.class);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(mainListener);
        Rect2i region = Rect2i.createFromMinAndSize(menuPosition, canvas.calculatePreferredSize(menu));
        canvas.drawWidget(menu, region);
    }

    @Override
    public void onClosed() {
        selectionListeners.forEach(UpdateListener::onAction);
    }

    public void setList(List list) {
        menu.setList(list);
    }

    public void bindSelection(Binding selection) {
        menu.bindSelection(selection);
    }

    public void setMenuPosition(Vector2i position) {
        menuPosition = position;
    }

    public void subscribeSelection(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        selectionListeners.add(listener);
    }

    public void unsubscribeSelection(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        selectionListeners.remove(listener);
    }
}
