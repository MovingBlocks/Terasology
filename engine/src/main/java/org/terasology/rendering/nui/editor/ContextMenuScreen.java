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
package org.terasology.rendering.nui.editor;

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

public class ContextMenuScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:contextMenuScreen");

    private UIList menu;
    private Vector2i menuPosition = Vector2i.zero();
    private List<UpdateListener> closeListeners = Lists.newArrayList();

    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            getManager().closeScreen(ASSET_URI);
            return false;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            getManager().closeScreen(ASSET_URI);
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
        closeListeners.forEach(UpdateListener::onChange);
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

    public void subscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.add(listener);
    }

    public void unsubscribe(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.remove(listener);
    }
}
