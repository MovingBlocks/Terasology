// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.editor.layers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.assets.ResourceUrn;
import org.terasology.nui.UILayout;
import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UpdateListener;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeValue;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.editor.utils.NUIEditorNodeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A screen to select a widget from the list of available widgets
 * and add it as a child of a specified {@link JsonTree}.
 */
@SuppressWarnings("unchecked")
public class WidgetSelectionScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:widgetSelectionScreen");

    /**
     * The dropdown containing the values of {@code widgets}.
     */
    private UIDropdownScrollable<String> availableWidgets;
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

        List<String> options = Lists.newArrayList(widgets.keySet());
        Collections.sort(options);
        availableWidgets.setOptions(options);

        // Add the widget as a child of the node.
        WidgetUtil.trySubscribe(this, "ok", button -> {
            String selection = availableWidgets.getSelection();
            JsonTree childNode;
            if (node.getValue().getType() == JsonTreeValue.Type.ARRAY) {
                ClassMetadata metadata = widgets.get(selection);

                // Get the widget tree from a utility method.
                childNode = NUIEditorNodeUtils.createNewWidget(selection, "newWidget", false);

                // If the widget is an UILayout override, also add a "contents" array node to the tree.
                if (UILayout.class.isAssignableFrom(metadata.getType())) {
                    childNode.addChild(new JsonTreeValue("contents", null, JsonTreeValue.Type.ARRAY));
                }
            } else {
                childNode = new JsonTree(new JsonTreeValue(selection, null, JsonTreeValue.Type.OBJECT));
                childNode.setExpanded(true);
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

        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().closeScreen(ASSET_URI));
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
