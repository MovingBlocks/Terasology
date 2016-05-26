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
package org.terasology.rendering.nui.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.models.Tree;
import org.terasology.rendering.nui.widgets.models.TreeModel;

/**
 *
 */
public class UITreeView<T> extends CoreWidget {
    private static final Logger logger = LoggerFactory.getLogger(UITreeView.class);
    private static final String TREE = "tree";

    private Binding<Integer> indentation = new DefaultBinding<>(25);
    private Binding<TreeModel> model = new DefaultBinding<>(new TreeModel<>());
    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();

    public UITreeView() {
    }

    public UITreeView(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(TREE);

        int yOffset = 0;
        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);
            TreeModel.TreeInteractionListener listener = model.get().getListener(i);
            if (listener.isMouseOver()) {
                canvas.setMode(HOVER_MODE);
            } else {
                canvas.setMode(DEFAULT_MODE);
            }

            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item.getValue(), canvas));
            Rect2i itemRegion = Rect2i.createFromMinAndSize(indentation.get() * item.getDepth(), yOffset, canvas.size().x, preferredSize.y);
            canvas.drawBackground(itemRegion);

            itemRenderer.draw(item.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));
            canvas.addInteractionRegion(listener, itemRenderer.getTooltip(item.getValue()), itemRegion);

            yOffset += preferredSize.getY();
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE);

        Vector2i result = new Vector2i();
        model.get().setEnumerateExpandedOnly(false);
        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);

            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item.getValue(), canvas));
            result.x = Math.max(result.x, preferredSize.x);
            result.y += preferredSize.y;
        }
        model.get().setEnumerateExpandedOnly(true);
        return result;
    }

    public void setModel(Tree<T> root) {
        this.setModel(new TreeModel<T>(root));
    }

    public void setModel(TreeModel<T> model) {
        this.model.set(model);
    }
}
