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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.MouseInput;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.models.Tree;
import org.terasology.rendering.nui.widgets.models.TreeModel;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public class UITreeView<T> extends CoreWidget {
    private static final Logger logger = LoggerFactory.getLogger(UITreeView.class);
    private static final String TREE_ITEM = "tree-item";
    private Binding<TreeModel<T>> model = new DefaultBinding<>(new TreeModel<>());
    private Binding<Tree<T>> selection = new DefaultBinding<>();

    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(Boolean.TRUE);

    private Binding<Integer> itemIndent = new DefaultBinding<>(15);
    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();

    private final List<TreeInteractionListener> itemListeners = Lists.newArrayList();

    public UITreeView() {
    }

    public UITreeView(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateItemListeners();

        canvas.setPart(TREE_ITEM);
        int yOffset = 0;
        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);
            TreeInteractionListener listener = itemListeners.get(i);
            if (Objects.equals(item, selection.get())) {
                canvas.setMode(ACTIVE_MODE);
            } else if (listener.isMouseOver()) {
                canvas.setMode(HOVER_MODE);
            } else {
                canvas.setMode(DEFAULT_MODE);
            }

            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item.getValue(), canvas));
            Rect2i itemRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(), yOffset, canvas.size().x - item.getDepth() * itemIndent.get(), preferredSize.y);
            canvas.drawBackground(itemRegion);

            itemRenderer.draw(item.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));
            canvas.addInteractionRegion(listener, itemRenderer.getTooltip(item.getValue()), itemRegion);

            yOffset += preferredSize.getY();
        }
    }

    private void updateItemListeners() {
        while (itemListeners.size() > model.get().getElementCount()) {
            itemListeners.remove(itemListeners.size() - 1);
        }
        while (itemListeners.size() < model.get().getElementCount()) {
            itemListeners.add(new TreeInteractionListener(itemListeners.size()));
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE_ITEM);
        model.get().setEnumerateExpandedOnly(false);
        Vector2i result = new Vector2i();
        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);
            Vector2i preferredSize = canvas.getCurrentStyle().getMargin()
                    .grow(itemRenderer.getPreferredSize(item.getValue(), canvas).addX(item.getDepth() * itemIndent.get()));
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

    public class TreeInteractionListener extends BaseInteractionListener {
        private int index;

        public TreeInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                model.get().getElement(index).setExpanded(!model.get().getElement(index).isExpanded());
                model.get().resetElements(model.get().getElement(index).getRoot());
                return true;
            }
            return false;
        }
    }
}
