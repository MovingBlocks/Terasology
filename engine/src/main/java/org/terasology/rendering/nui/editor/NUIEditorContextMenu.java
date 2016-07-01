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

import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.Arrays;

public class NUIEditorContextMenu extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiEditorContextMenu");

    private UIList menu;
    private Vector2i menuPosition = Vector2i.zero();

    @Override
    public void initialise() {
        menu = find("menu", UIList.class);
        menu.setList(Arrays.asList("Test1", "Test2"));
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect2i region =
                Rect2i.createFromMinAndSize(menuPosition, menu.getPreferredContentSize(canvas, null));
        canvas.drawWidget(menu, region);
    }

    public void setMenuPosition(Vector2i position) {
        menuPosition = position;
    }
}
