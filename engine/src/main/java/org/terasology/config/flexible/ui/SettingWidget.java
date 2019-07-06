/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.config.flexible.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.asset.UIElement;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public abstract class SettingWidget<C extends SettingConstraint<?>> extends CoreWidget {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingWidget.class);
    private final String contentsUri;
    protected UIWidget contents;
    private Setting<?> setting;

    protected SettingWidget(String contentsUri) {
        this.contentsUri = contentsUri;
    }

    public abstract void initialise();

    @Override
    public Iterator<UIWidget> iterator() {
        if (contents == null) {
            return Collections.emptyIterator();
        }
        return Collections.singletonList(contents).iterator();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawWidget(contents);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    protected Setting<?> getSetting() {
        return setting;
    }

    public final void bindToSetting(Setting<?> setting) {
        this.setting = setting;
        initialise();
    }

    public final void loadContents(AssetManager assetManager) {
        Optional<UIElement> uiElement = assetManager.getAsset(contentsUri, UIElement.class);

        if (!uiElement.isPresent()) {
            LOGGER.error("Can't find unique UI element '{}'", contentsUri);
        } else {
            this.contents = uiElement.get().getRootWidget();
        }
    }

    @SuppressWarnings({"unchecked"})
    protected C getConstraint() {
        return (C) setting.getConstraint();
    }
}
