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
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;

public class NUIEditorSettingsScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiEditorSettingsScreen");

    @In
    private Config config;

    @Override
    public void initialise() {
        WidgetUtil.tryBindCheckbox(this, "disableIcons", BindHelper.bindBeanProperty("disableIcons", config.getNuiEditor(), Boolean.TYPE));
        WidgetUtil.trySubscribe(this, "close", button -> getManager().closeScreen(ASSET_URI));
    }

    @Override
    public void onClosed() {
        if (getManager().isOpen(NUIEditorScreen.ASSET_URI)) {
            ((NUIEditorScreen) getManager().getScreen(NUIEditorScreen.ASSET_URI)).updateConfig();
        }
        if (getManager().isOpen(NUISkinEditorScreen.ASSET_URI)) {
            ((NUISkinEditorScreen) getManager().getScreen(NUISkinEditorScreen.ASSET_URI)).updateConfig();
        }
    }
}
