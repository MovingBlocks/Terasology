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
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.assets.ResourceUrn;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.rendering.nui.editor.systems.NUISkinEditorSystem;

/**
 *
 */
public class DevToolsMenuScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:devToolsMenuScreen");

    @In
    private NUIEditorSystem nuiEditorSystem;
    @In
    private NUISkinEditorSystem nuiSkinEditorSystem;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "nuiEditor", button -> nuiEditorSystem.toggleEditor());
        WidgetUtil.trySubscribe(this, "nuiSkinEditor", button -> nuiSkinEditorSystem.toggleEditor());
        WidgetUtil.trySubscribe(this, "btEditor", button -> getManager().toggleScreen("engine:behaviorEditorScreen"));
        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
