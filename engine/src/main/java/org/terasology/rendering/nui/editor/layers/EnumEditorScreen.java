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
package org.terasology.rendering.nui.editor.layers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UpdateListener;
import org.terasology.nui.widgets.treeView.JsonTree;

import java.util.Arrays;
import java.util.List;

/**
 * A popup to edit a {@link JsonTree} with an enum type.
 */
@SuppressWarnings("unchecked")
public class EnumEditorScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:enumEditorScreen");

    /**
     * The dropdown containing the possible values of an enum.
     */
    private UIDropdownScrollable enumValues;
    /**
     * The node to be edited.
     */
    private JsonTree node;
    /**
     * Listeners fired when the screen is closed (without selecting a widget).
     */
    private List<UpdateListener> closeListeners = Lists.newArrayList();

    @Override
    public void initialise() {
        enumValues = find("enumValues", UIDropdownScrollable.class);

        WidgetUtil.trySubscribe(this, "ok", button -> {
            // Apply the changes to the node.
            node.getValue().setValue(enumValues.getSelection().toString());
            closeListeners.forEach(UpdateListener::onAction);
            getManager().closeScreen(ASSET_URI);
        });

        find("ok", UIButton.class).bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                // Disable the OK button if no changes were made.
                return enumValues.getSelection() != null;
            }
        });

        WidgetUtil.trySubscribe(this, "cancel", button -> {
            getManager().closeScreen(ASSET_URI);
        });
    }

    public void setNode(JsonTree node) {
        this.node = node;
    }

    public void setEnumClass(Class clazz) {
        enumValues.setOptions(Arrays.asList(clazz.getEnumConstants()));
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
