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
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDoubleClickEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.models.TreeViewModel;

import java.util.List;

/**
 *
 */
public class UITreeView<T> extends CoreWidget {
    private static final Logger logger = LoggerFactory.getLogger(UITreeView.class);
    private static final String TREE = "tree";

    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(Boolean.TRUE);

    private Binding<TreeViewModel<T>> model = new DefaultBinding<>(new TreeViewModel<T>());
    private Binding<T> selection = new DefaultBinding<>();

    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();

    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            logger.info("mainListener");
            return false;
        }
    };

    public UITreeView() {

    }

    public UITreeView(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(TREE);
        List<T> items = model.get().getItems(true);

        int yOffset = 0;
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);

            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item, canvas));
            Rect2i itemRegion = Rect2i.createFromMinAndSize(0, yOffset, canvas.size().x, preferredSize.y);
            canvas.drawBackground(itemRegion);

            itemRenderer.draw(item, canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));
            canvas.addInteractionRegion(new ItemInteractionListener(), itemRenderer.getTooltip(item), itemRegion);

            yOffset += preferredSize.getY();
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE);
        List<T> items = model.get().getItems(false);

        Vector2i result = new Vector2i();
        for (T item : items) {
            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item, canvas));
            result.x = Math.max(result.x, preferredSize.x);
            result.y += preferredSize.y;
        }
        return result;
    }

    public void setModel(TreeViewModel<T> model) {
        this.model.set(model);
    }

    private class ItemInteractionListener extends BaseInteractionListener {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            logger.info("itemListener mouseClick");
            return false;
        }

        @Override
        public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
            logger.info("itemListener mouseDoubleClick");
            return false;
        }
    }
}
