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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDoubleClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.treeView.Tree;
import org.terasology.rendering.nui.widgets.treeView.TreeKeyEventListener;
import org.terasology.rendering.nui.widgets.treeView.TreeModel;
import org.terasology.rendering.nui.widgets.treeView.TreeMouseClickListener;
import org.terasology.rendering.nui.widgets.treeView.TreeViewState;

import java.util.List;

/**
 * A Tree View widget. Presents a hierarchical view of items, visualised by indentation.
 *
 * @param <T> Type of objects stored in the underlying tree.
 */
public class UITreeView<T> extends CoreWidget {
    public enum MouseOverType {
        TOP,
        CENTER,
        BOTTOM
    }

    // Canvas parts.
    private static final String EXPAND_BUTTON = "expand-button";
    private static final String TREE_NODE = "tree-node";

    // Canvas modes.
    private static final String CONTRACT_MODE = "contract";
    private static final String CONTRACT_HOVER_MODE = "contract-hover";
    private static final String EXPAND_MODE = "expand";
    private static final String EXPAND_HOVER_MODE = "expand-hover";
    private static final String HOVER_DISABLED_MODE = "hover-disabled";

    /**
     * The horizontal indentation, in pixels, corresponding to one level in the tree.
     */
    @LayoutConfig
    private Binding<Integer> levelIndent = new DefaultBinding<>(25);
    /**
     * The underlying tree model - a wrapper around a {@code Tree<T>}.
     */
    private Binding<TreeModel<T>> model = new DefaultBinding<>(new TreeModel<>());
    /**
     * The state of the tree - includes session-specific information about the currently selected node, copied node etc.
     * See the documentation {@link TreeViewState} for information concerning specific state variables.
     */
    private TreeViewState<T> state = new TreeViewState<>();
    /**
     * The item renderer used for drawing the values of the tree.
     */
    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();
    /**
     * Listeners fired when a node is clicked. Internally instantiated.
     */
    private final List<TreeViewListenerSet> treeViewListenerSets = Lists.newArrayList();
    /**
     * Listeners fired when the expand/contract button is clicked.
     */
    private final List<ExpandButtonInteractionListener> expandListeners = Lists.newArrayList();
    /**
     * Listeners fired when the model of the tree is updated.
     */
    private List<UpdateListener> updateListeners = Lists.newArrayList();
    /**
     * Listeners fired when a node is clicked. Can be subscribed to.
     */
    private List<TreeMouseClickListener> nodeClickListeners = Lists.newArrayList();
    /**
     * Listeners fired when a node is double-clicked. Can be subscribed to.
     */
    private List<TreeMouseClickListener> nodeDoubleClickListeners = Lists.newArrayList();
    /**
     * Listeners fired when a key is pressed. Can be subscribed to.
     */
    private List<TreeKeyEventListener> keyEventListeners = Lists.newArrayList();

    public UITreeView() {
    }

    public UITreeView(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateListeners();

        canvas.setPart(TREE_NODE);

        int currentHeight = 0;
        for (int i = 0; i < model.get().getNodeCount(); i++) {
            Tree<T> node = model.get().getNode(i);
            TreeViewListenerSet treeViewListenerSet = treeViewListenerSets.get(i);
            ExpandButtonInteractionListener buttonListener = expandListeners.get(i);

            // Calculate the node's height and overall region.
            int nodeHeight = canvas.getCurrentStyle().getMargin()
                .grow(itemRenderer.getPreferredSize(node.getValue(), canvas).addX(node.getDepth() * levelIndent.get()))
                .getY();

            Rect2i nodeRegion = Rect2i.createFromMinAndSize((node.getDepth() + 1) * levelIndent.get(),
                currentHeight,
                canvas.size().x - (node.getDepth() + 1) * levelIndent.get(),
                nodeHeight);

            // Draw the expand/contract button.
            if (!node.isLeaf()) {
                canvas.setPart(EXPAND_BUTTON);

                setButtonMode(canvas, node, buttonListener);
                Rect2i buttonRegion = Rect2i.createFromMinAndSize(node.getDepth() * levelIndent.get(),
                    currentHeight,
                    levelIndent.get(),
                    nodeHeight);
                drawButton(canvas, buttonRegion, buttonListener);

                canvas.setPart(TREE_NODE);
            }

            if (state.getSelectedIndex() != null && state.getSelectedIndex() == i
                && state.getAlternativeWidget() != null) {
                //Draw an alternative widget in place of the node (with the same size).

                canvas.drawWidget(state.getAlternativeWidget(), nodeRegion);
                currentHeight += nodeHeight;
            } else {
                // Draw the node itself.
                setNodeMode(canvas, node, treeViewListenerSet);

                drawNode(canvas, nodeRegion, node, treeViewListenerSet);
                currentHeight += nodeHeight;

                // Draw the dragging hints if the current node is a drag&drop target.
                if (state.getMouseOverIndex() != null && state.getMouseOverIndex() == i) {
                    drawDragHint(canvas, nodeRegion);
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE_NODE);

        if (model.get().getNodeCount() == 0) {
            return new Vector2i();
        }

        model.get().setEnumerateExpandedOnly(false);
        Vector2i result = new Vector2i();
        for (int i = 0; i < model.get().getNodeCount(); i++) {
            Tree<T> node = model.get().getNode(i);
            Vector2i preferredSize = canvas.getCurrentStyle().getMargin()
                .grow(itemRenderer.getPreferredSize(node.getValue(), canvas)
                    .addX(node.getDepth() * levelIndent.get()));
            result.x = Math.max(result.x, preferredSize.x);
            result.y += preferredSize.y;
        }
        model.get().setEnumerateExpandedOnly(true);

        // Account for the expand/contract button!
        result.addX(levelIndent.get());

        return result;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (state.getAlternativeWidget() != null) {
            state.getAlternativeWidget().update(delta);
        }
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        for (TreeKeyEventListener listener : keyEventListeners) {
            listener.onKeyEvent(event);
        }

        if (event.isDown()) {
            int id = event.getKey().getId();
            KeyboardDevice keyboard = event.getKeyboard();
            boolean ctrlDown = keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL);

            if (id == Keyboard.KeyId.UP || id == Keyboard.KeyId.DOWN) {
                // Up/Down: change a node's position within the parent node.
                return moveSelected(id);
            } else if (id == Keyboard.KeyId.DELETE) {
                // Delete: remove a node (and all its' children).
                return removeSelected();
            } else if (ctrlDown && id == Keyboard.KeyId.C) {
                // Ctrl+C: copy a selected node.
                if (state.getSelectedIndex() != null) {
                    copy(model.get().getNode(state.getSelectedIndex()));
                    return true;
                }
                return false;
            } else if (ctrlDown && id == Keyboard.KeyId.V) {
                // Ctrl+V: paste the copied node as a child of the currently selected node.
                if (state.getSelectedIndex() != null) {
                    paste(model.get().getNode(state.getSelectedIndex()));
                    return true;
                }
                return false;
            } else {
                return false;
            }
        }

        return false;
    }

    public Integer getSelectedIndex() {
        return state.getSelectedIndex();
    }

    public void setSelectedIndex(Integer index) {
        state.setSelectedIndex(index);
    }

    public UIWidget getAlternativeWidget() {
        return state.getAlternativeWidget();
    }

    public void setAlternativeWidget(UIWidget widget) {
        state.setAlternativeWidget(widget);
    }

    public void fireUpdateListeners() {
        state.setAlternativeWidget(null);
        updateListeners.forEach(UpdateListener::onAction);
    }

    public void copy(Tree<T> node) {
        state.setClipboard(node.copy());
    }

    public void paste(Tree<T> node) {
        if (state.getClipboard() != null) {
            node.addChild(state.getClipboard());

            fireUpdateListeners();
        }
    }

    public void delete(Tree<T> node) {
        if (node.getParent() != null) {
            node.getParent().removeChild(node);

            fireUpdateListeners();
        }
    }

    public TreeModel<T> getModel() {
        return model.get();
    }

    public void setModel(Tree<T> root) {
        setModel(new TreeModel<>(root));
    }

    public void setModel(TreeModel<T> newModel) {
        model.set(newModel);
        state.setAlternativeWidget(null);
        state.setSelectedIndex(null);
    }

    public void setItemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    public void subscribeTreeViewUpdate(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        updateListeners.add(listener);
    }

    public void subscribeNodeClick(TreeMouseClickListener listener) {
        Preconditions.checkNotNull(listener);
        nodeClickListeners.add(listener);
    }

    public void subscribeNodeDoubleClick(TreeMouseClickListener listener) {
        Preconditions.checkNotNull(listener);
        nodeDoubleClickListeners.add(listener);
    }

    public void subscribeKeyEvent(TreeKeyEventListener listener) {
        Preconditions.checkNotNull(listener);
        keyEventListeners.add(listener);
    }

    private void setButtonMode(Canvas canvas, Tree<T> node, ExpandButtonInteractionListener listener) {
        if (listener.isMouseOver()) {
            canvas.setMode(node.isExpanded() ? CONTRACT_HOVER_MODE : EXPAND_HOVER_MODE);
        } else {
            canvas.setMode(node.isExpanded() ? CONTRACT_MODE : EXPAND_MODE);
        }
    }

    private void setNodeMode(Canvas canvas, Tree<T> node, TreeViewListenerSet listenerSet) {
        if (state.getSelectedIndex() != null
            && node.equals(model.get().getNode(state.getSelectedIndex()))) {
            canvas.setMode(ACTIVE_MODE);
        } else if (listenerSet.isMouseOver()) {
            canvas.setMode(isEnabled() ? HOVER_MODE : HOVER_DISABLED_MODE);
        } else if (!isEnabled()) {
            canvas.setMode(DISABLED_MODE);
        } else {
            canvas.setMode(DEFAULT_MODE);
        }
    }

    private void drawButton(Canvas canvas, Rect2i buttonRegion, ExpandButtonInteractionListener listener) {
        canvas.drawBackground(buttonRegion);
        canvas.addInteractionRegion(listener, buttonRegion);
    }

    private void drawNode(Canvas canvas, Rect2i nodeRegion, Tree<T> node, TreeViewListenerSet listenerSet) {
        canvas.drawBackground(nodeRegion);
        itemRenderer.draw(node.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(nodeRegion));

        // Add the top listener.
        canvas.addInteractionRegion(listenerSet.getTopListener(), itemRenderer.getTooltip(node.getValue()),
            Rect2i.createFromMinAndSize(nodeRegion.minX(), nodeRegion.minY(),
                nodeRegion.width(), nodeRegion.height() / 3));

        // Add the central listener.
        canvas.addInteractionRegion(listenerSet.getCenterListener(), itemRenderer.getTooltip(node.getValue()),
            Rect2i.createFromMinAndSize(nodeRegion.minX(), nodeRegion.minY() + nodeRegion.height() / 3,
                nodeRegion.width(), nodeRegion.height() / 3));

        int heightOffset = nodeRegion.height() - 3 * (nodeRegion.height() / 3);

        // Add the bottom listener.
        canvas.addInteractionRegion(listenerSet.getBottomListener(), itemRenderer.getTooltip(node.getValue()),
            Rect2i.createFromMinAndSize(nodeRegion.minX(), nodeRegion.minY() + 2 * nodeRegion.height() / 3,
                nodeRegion.width(), heightOffset + nodeRegion.height() / 3));
    }

    private void drawDragHint(Canvas canvas, Rect2i nodeRegion) {
        if (state.getMouseOverType() == MouseOverType.TOP) {
            // Draw a line at the top of the node.
            canvas.drawLine(nodeRegion.minX(), nodeRegion.minY(), nodeRegion.maxX(), nodeRegion.minY(), Color.WHITE);
        } else if (state.getMouseOverType() == MouseOverType.CENTER) {
            // Draw a border around the node.
            canvas.drawLine(nodeRegion.minX(), nodeRegion.minY(), nodeRegion.maxX(), nodeRegion.minY(), Color.WHITE);
            canvas.drawLine(nodeRegion.maxX(), nodeRegion.minY(), nodeRegion.maxX(), nodeRegion.maxY(), Color.WHITE);
            canvas.drawLine(nodeRegion.minX(), nodeRegion.minY(), nodeRegion.minX(), nodeRegion.maxY(), Color.WHITE);
            canvas.drawLine(nodeRegion.minX(), nodeRegion.maxY(), nodeRegion.maxX(), nodeRegion.maxY(), Color.WHITE);
        } else { // MouseOverType.BOTTOM
            // Draw a line at the bottom of the node.
            canvas.drawLine(nodeRegion.minX(), nodeRegion.maxY(), nodeRegion.maxX(), nodeRegion.maxY(), Color.WHITE);
        }
    }

    private void updateListeners() {
        boolean mouseOver = false;
        for (TreeViewListenerSet set : treeViewListenerSets) {
            if (set.isMouseOver()) {
                mouseOver = true;
                break;
            }
        }
        if (!mouseOver) {
            // Reset the temporary index variables.
            if (state.getDraggedIndex() != null) {
                state.setDraggedIndex(null);
            }
            if (state.getMouseOverIndex() != null) {
                state.setMouseOverIndex(null);
                state.setMouseOverType(null);
            }
        }

        // Update the listener sets.
        while (treeViewListenerSets.size() > model.get().getNodeCount()) {
            treeViewListenerSets.remove(treeViewListenerSets.size() - 1);
            expandListeners.remove(expandListeners.size() - 1);
        }
        while (treeViewListenerSets.size() < model.get().getNodeCount()) {
            treeViewListenerSets.add(new TreeViewListenerSet(
                new NodeTopListener(treeViewListenerSets.size()),
                new NodeCenterListener(treeViewListenerSets.size()),
                new NodeBottomListener(treeViewListenerSets.size())));
            expandListeners.add(new ExpandButtonInteractionListener(expandListeners.size()));
        }
    }

    private boolean moveSelected(int keyId) {
        if (state.getSelectedIndex() != null) {
            Tree<T> selectedNode = model.get().getNode(state.getSelectedIndex());
            Tree<T> parent = selectedNode.getParent();

            if (!selectedNode.isRoot()) {
                int nodeIndex = parent.getIndex(selectedNode);

                if (keyId == Keyboard.KeyId.UP && nodeIndex > 0) {
                    // Move the node up, unless it is the first node.
                    parent.removeChild(selectedNode);
                    parent.addChild(nodeIndex - 1, selectedNode);
                    model.get().resetNodes();

                    // Re-select the moved node.
                    state.setSelectedIndex(model.get().indexOf(selectedNode));

                    fireUpdateListeners();
                } else if (keyId == Keyboard.KeyId.DOWN && nodeIndex < parent.getChildren().size() - 1) {
                    // Move the node down, unless it is the last node.
                    parent.removeChild(selectedNode);
                    parent.addChild(nodeIndex + 1, selectedNode);
                    model.get().resetNodes();

                    // Re-select the moved node.
                    state.setSelectedIndex(model.get().indexOf(selectedNode));

                    fireUpdateListeners();
                }
            }

            return true;
        }

        return false;
    }

    private boolean removeSelected() {
        if (state.getSelectedIndex() != null) {
            model.get().removeNode(state.getSelectedIndex());
            state.setSelectedIndex(null);

            fireUpdateListeners();
            return true;
        }
        return false;
    }

    private boolean onNodeClick(int index, NUIMouseClickEvent event) {
        for (TreeMouseClickListener listener : nodeClickListeners) {
            listener.onMouseClick(event, model.get().getNode(index));
        }
        if (isEnabled() && event.getMouseButton() == MouseInput.MOUSE_LEFT) {
            // Select the node on LMB - deselect when selected again.
            if (state.getSelectedIndex() != null && state.getSelectedIndex() == index) {
                state.setSelectedIndex(null);
            } else {
                state.setSelectedIndex(index);
            }
            state.setAlternativeWidget(null);
            return true;
        }
        return false;
    }

    private boolean onNodeDoubleClick(int index, NUIMouseDoubleClickEvent event) {
        for (TreeMouseClickListener listener : nodeDoubleClickListeners) {
            listener.onMouseClick(event, model.get().getNode(index));
        }
        return true;
    }

    private void onNodeMouseDrag(int index) {
        state.setDraggedIndex(index);
    }

    private void onNodeMouseOver(int index, MouseOverType type) {
        // Set temporary index variables for the dragged/target nodes.
        if (state.getDraggedIndex() != null) {
            if (state.getDraggedIndex() != index) {
                state.setMouseOverIndex(index);
                state.setMouseOverType(type);
            } else {
                state.setMouseOverIndex(null);
                state.setMouseOverType(null);
            }
        }
    }

    private void onNodeMouseRelease(int index) {
        if (state.getDraggedIndex() != null && state.getMouseOverIndex() != null) {
            Tree<T> child = model.get().getNode(state.getDraggedIndex());
            Tree<T> parent = model.get().getNode(state.getMouseOverIndex());

            // Handle node drag&dropping.
            if (state.getMouseOverType() == MouseOverType.TOP) {
                // Insert the dragged node before the target node (as a child of the same tree).
                child.getParent().removeChild(child);
                parent.getParent().addChild(parent.getParent().indexOf(parent), child);
            } else if (state.getMouseOverType() == MouseOverType.CENTER) {
                // Insert the dragged node as a child of the target node.
                child.getParent().removeChild(child);
                parent.addChild(child);
            } else { // MouseOverType.BOTTOM
                // Insert the dragged node after the target node (as a child of the same tree).
                child.getParent().removeChild(child);
                parent.getParent().addChild(parent.getParent().indexOf(parent) + 1, child);
            }

            fireUpdateListeners();
        }

        // Reset the temporary index variables.
        if (state.getDraggedIndex() != null) {
            if (state.getMouseOverIndex() != null && state.getMouseOverIndex() != index) {
                state.setSelectedIndex(null);
            }
            state.setDraggedIndex(null);
        }
        if (state.getMouseOverIndex() != null) {
            state.setMouseOverIndex(null);
            state.setMouseOverType(null);
        }
    }

    private class ExpandButtonInteractionListener extends BaseInteractionListener {
        private int index;

        ExpandButtonInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                // Expand or contract a node on LMB - works even if the tree is disabled.
                model.get().getNode(index).setExpanded(!model.get().getNode(index).isExpanded());

                Tree<T> selectedNode = state.getSelectedIndex() != null ? model.get().getNode(state.getSelectedIndex()) : null;
                model.get().resetNodes();

                // Update the index of the selected node.
                if (selectedNode != null) {
                    int newIndex = model.get().indexOf(selectedNode);
                    if (newIndex == -1) {
                        state.setSelectedIndex(null);
                    } else {
                        state.setSelectedIndex(newIndex);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private class NodeTopListener extends BaseInteractionListener {
        private int index;

        NodeTopListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return onNodeClick(index, event);
        }

        @Override
        public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
            return onNodeDoubleClick(index, event);
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            onNodeMouseDrag(index);
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);

            // This node's parent exists and accepts the node being dragged as a child.
            if (state.getDraggedIndex() != null
                && !model.get().getNode(index).isRoot()
                && model.get().getNode(index).getParent().acceptsChild(model.get().getNode(state.getDraggedIndex()))) {
                onNodeMouseOver(index, MouseOverType.TOP);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onNodeMouseRelease(index);
        }
    }

    private class NodeCenterListener extends BaseInteractionListener {
        private int index;

        NodeCenterListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return onNodeClick(index, event);
        }

        @Override
        public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
            return onNodeDoubleClick(index, event);
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            onNodeMouseDrag(index);
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);

            // This node accepts the node being dragged as a child.
            if (state.getDraggedIndex() != null
                && model.get().getNode(index).acceptsChild(model.get().getNode(state.getDraggedIndex()))) {
                onNodeMouseOver(index, MouseOverType.CENTER);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onNodeMouseRelease(index);
        }
    }

    private class NodeBottomListener extends BaseInteractionListener {
        private int index;

        NodeBottomListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return onNodeClick(index, event);
        }

        @Override
        public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
            return onNodeDoubleClick(index, event);
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            onNodeMouseDrag(index);
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);

            // This node's parent exists and accepts the node being dragged as a child.
            if (state.getDraggedIndex() != null
                && !model.get().getNode(index).isRoot()
                && model.get().getNode(index).getParent().acceptsChild(model.get().getNode(state.getDraggedIndex()))) {
                onNodeMouseOver(index, MouseOverType.BOTTOM);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onNodeMouseRelease(index);
        }
    }

    /**
     * A set of tree node sub-listeners.
     */
    private final class TreeViewListenerSet {
        /**
         * The top listener.
         */
        private NodeTopListener topListener;
        /**
         * The central listener.
         */
        private NodeCenterListener centerListener;
        /**
         * The bottom listener.
         */
        private NodeBottomListener bottomListener;

        private TreeViewListenerSet(NodeTopListener topListener, NodeCenterListener centerListener, NodeBottomListener bottomListener) {
            this.topListener = topListener;
            this.centerListener = centerListener;
            this.bottomListener = bottomListener;
        }

        /**
         * @return The top listener.
         */
        NodeTopListener getTopListener() {
            return topListener;
        }

        /**
         * @return The central listener.
         */
        NodeCenterListener getCenterListener() {
            return centerListener;
        }

        /**
         * @return The bottom listener.
         */
        NodeBottomListener getBottomListener() {
            return bottomListener;
        }

        /**
         * @return Whether any of the listeners are currently moused over.
         */
        public boolean isMouseOver() {
            return topListener.isMouseOver() || centerListener.isMouseOver() || bottomListener.isMouseOver();
        }
    }
}
