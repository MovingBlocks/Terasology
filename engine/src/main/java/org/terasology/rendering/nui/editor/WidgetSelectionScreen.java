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
import com.google.common.collect.Maps;
import org.terasology.assets.ResourceUrn;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UILayout;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UpdateListener;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WidgetSelectionScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:widgetSelectionScreen");

    /**
     * The dropdown containing the values of {@code widgets}.
     */
    private UIDropdownScrollable availableWidgets;
    /**
     * The {@link JsonTree} a selected widget is to be added to.
     */
    private JsonTree node;
    /**
     * The list of available UIWidget instances, excluding CoreScreenLayer overrides.
     */
    private Map<String, ClassMetadata> widgets = Maps.newHashMap();
    /**
     * Listeners fired when the screen is closed (without selecting a widget).
     */
    private List<UpdateListener> closeListeners = Lists.newArrayList();

    @Override
    public void initialise() {
        availableWidgets = find("availableWidgets", UIDropdownScrollable.class);

        // Populate the widget list.
        ClassLibrary<UIWidget> metadataLibrary = getManager().getWidgetMetadataLibrary();
        for (ClassMetadata metadata : metadataLibrary) {
            if (!CoreScreenLayer.class.isAssignableFrom(metadata.getType())) {
                widgets.put(metadata.toString(), metadata);
            }
        }

        List options = Lists.newArrayList(widgets.keySet());
        Collections.sort(options);
        availableWidgets.setOptions(options);

        WidgetUtil.trySubscribe(this, "ok", button -> {
            String selection = availableWidgets.getSelection().toString();

            ClassMetadata metadata = widgets.get(selection);

            JsonTree childNode = NUIEditorTemplateUtils.newWidget(selection, "newWidget");

            // If the widget is an UILayout override, add a "contents" array node.
            if (UILayout.class.isAssignableFrom(metadata.getType())) {
                childNode.addChild(new JsonTreeValue("contents", null, JsonTreeValue.Type.ARRAY));
            }

            node.addChild(childNode);

            closeListeners.forEach(UpdateListener::onAction);
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

    public void setNode(JsonTree node) {
        this.node = node;
    }

    public void subscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.add(listener);
    }

    public void unsubscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.remove(listener);
    }
}
