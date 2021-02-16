// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.assets.ResourceUrn;
import org.terasology.nui.WidgetUtil;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
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
