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
import org.terasology.assets.ResourceUrn;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.models.JsonTree;

import java.util.List;

public class WidgetSelectionScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:widgetSelectionScreen");

    private UIDropdownScrollable availableWidgets;

    private JsonTree item;

    @Override
    public void initialise() {
        availableWidgets = find("availableWidgets", UIDropdownScrollable.class);

        List<Object> options = Lists.newArrayList();
        ClassLibrary<UIWidget> metadataLibrary = getManager().getWidgetMetadataLibrary();
        for (Object aMetadataLibrary : metadataLibrary) {
            options.add(aMetadataLibrary.toString());
        }
        availableWidgets.setOptions(options);

        WidgetUtil.trySubscribe(this, "ok", button -> {
            String selection = availableWidgets.getSelection().toString();

            // TODO: Create a JsonTree representing a default widget here.

            getManager().closeScreen(ASSET_URI);
        });

        find("ok", UIButton.class).bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return availableWidgets.getSelection() != null;
            }
        });

        WidgetUtil.trySubscribe(this, "cancel", button -> {
            getManager().closeScreen(ASSET_URI);
        });
    }

    public void setItem(JsonTree item) {
        this.item = item;
    }
}
