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
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.nui.widgets.UITreeView;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.contextMenu.ContextMenuUtils;
import org.terasology.rendering.nui.contextMenu.MenuTree;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeValue;
import org.terasology.nui.widgets.treeView.Tree;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Tree View widget designed to edit {@link JsonTree}s -
 * tree representations of a JSON object.
 */
public class JsonEditorTreeView extends UITreeView<JsonTreeValue> {
    /**
     * The list of this widget's model states.
     */
    private List<JsonTree> history = Lists.newArrayList();
    /**
     * The current position in the list of this widget's states.
     */
    private int historyPosition;
    /**
     * The function used to instantiate a {@link MenuTree} from a given node.
     */
    private Function<JsonTree, MenuTree> contextMenuTreeProducer;

    public JsonEditorTreeView() {
    }

    /**
     * @return The root node of this widget's model.
     */
    public JsonTree getRoot() {
        return (JsonTree) getModel().getNode(0).getRoot().copy();
    }

    /**
     * Adds the current root node of this widget's model to the state history.
     */
    public void addToHistory() {
        if (historyPosition < history.size() - 1) {
            history = history.subList(0, historyPosition + 1);
        }
        history.add(getRoot());
        historyPosition++;
    }

    /**
     * Clears the widget's state history.
     */
    public void clearHistory() {
        history.clear();
        historyPosition = 0;
        history.add(getRoot());
    }

    /**
     * Sets the widget's state to the previous item in the history.
     *
     * @return true if the widget's state was changed, false otherwise.
     */
    public boolean undo() {
        if (historyPosition > 0) {
            historyPosition--;
            JsonTree node = (JsonTree) history.get(historyPosition).copy();
            setTreeViewModel(node, false);
            return true;
        }
        return false;
    }

    /**
     * Sets the widget's state to the next item in the history.
     *
     * @return true if the widget's state was changed, false otherwise.
     */
    public boolean redo() {
        if (historyPosition < history.size() - 1) {
            historyPosition++;
            JsonTree node = (JsonTree) history.get(historyPosition).copy();
            setTreeViewModel(node, false);
            return true;
        }
        return false;
    }

    /**
     * Sets the widget's state to a copy of a specified {@link JsonTree}.
     *
     * @param node   The node the widget's state is to be set to.
     * @param expand Whether the node should be expanded.
     */
    public void setTreeViewModel(JsonTree node, boolean expand) {
        if (expand) {
            expandNode(node);
        }

        setModel(node.copy());
    }

    /**
     * Expands a {@link JsonTree} meeting specific conditions; repeats recursively for its' children.
     *
     * @param node The node to be expanded.
     */
    private void expandNode(JsonTree node) {
        // Do not expand OBJECT children of ARRAY parents. Generally concerns widget lists.
        if (!(node.getValue().getType() == JsonTreeValue.Type.OBJECT
              && !node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            node.setExpanded(true);
        }

        for (Tree child : node.getChildren()) {
            expandNode((JsonTree) child);
        }
    }

    /**
     * Copies the specified node to the editor's clipboard,
     * then deselects it.
     *
     * @param node The node to copy.
     */
    public void copyNode(JsonTree node) {
        copy(node);
        setSelectedIndex(null);
    }

    /**
     * Pastes the currently copied node as a child of the specified node,
     * then deselects it.
     *
     * @param node The node to paste the copied node to.
     */
    public void pasteNode(JsonTree node) {
        paste(node);
        setSelectedIndex(null);
    }

    public void deleteNode(JsonTree node) {
        delete(node);
        setSelectedIndex(null);
    }

    public void setContextMenuTreeProducer(Function<JsonTree, MenuTree> contextMenuTreeProducer) {
        this.contextMenuTreeProducer = contextMenuTreeProducer;
    }

    public void setEditor(Consumer<JsonTree> editorFunction, NUIManager manager) {
        // Create and display a context menu on RMB.
        subscribeNodeClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_RIGHT) {
                setSelectedIndex(getModel().indexOf(node));
                setAlternativeWidget(null);

                MenuTree menuTree = contextMenuTreeProducer.apply((JsonTree) node);

                ContextMenuUtils.showContextMenu(manager, event.getMouse().getPosition(), menuTree);
            }
        });

        // Edit a node on double click.
        subscribeNodeDoubleClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                editorFunction.accept((JsonTree) node);
            }
        });

        // Edit the currently selected node on F2.
        subscribeKeyEvent(event -> {
            if (event.isDown() && event.getKey() == Keyboard.Key.F2) {
                Integer selectedIndex = getSelectedIndex();

                if (selectedIndex != null) {
                    editorFunction.accept((JsonTree) getModel().getNode(selectedIndex));
                }
            }
        });
    }
}
