/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.layers.ingame;

import org.terasology.input.Keyboard;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.layouts.ZoomableLayout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphRenderer extends ZoomableLayout {

    private RenderGraph renderGraph;
    private Map<Node, RenderingNode> mapping = new HashMap<>();
    private float nodeWidth = 20f;
    private float nodeHeight = 3f;

    public RenderGraph getRenderGraph() {
        return renderGraph;
    }

    //TODO remove - debug handler
    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && event.getKey() == Keyboard.Key.NUMPAD_PLUS) {
            nodeWidth += 1f;
            getWidgets().forEach((w) -> w.getSize().addX(1f));
        }
        if (event.isDown() && event.getKey() == Keyboard.Key.NUMPAD_MINUS) {
            nodeWidth -= 1f;
            getWidgets().forEach((w) -> w.getSize().addX(-1f));
        }
        if (event.isDown() && event.getKey() == Keyboard.Key.NUMPAD_MULTIPLY) {
            nodeHeight += 1f;
            getWidgets().forEach((w) -> w.getSize().addY(1f));
        }
        if (event.isDown() && event.getKey() == Keyboard.Key.NUMPAD_DIVIDE) {
            nodeHeight -= 1f;
            getWidgets().forEach((w) -> w.getSize().addY(-1f));
        }
        System.out.println(nodeWidth + "x" + nodeHeight);
        return super.onKeyEvent(event);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PositionalWidget positionalWidget : getWidgets()) {
            RenderingNode widget1 = (RenderingNode) positionalWidget;
            Vector2f size = positionalWidget.getSize();
            Vector2f topLeft = positionalWidget.getPosition();
            Vector2f topRight = new Vector2f(topLeft).addX(size.x);
            Vector2f bottomLeft = new Vector2f(topLeft).addY(size.y);
            Vector2f bottomRight = new Vector2f(topLeft).add(new Vector2f(size.x, size.y));
            Color color;
            if (widget1.node.isEnabled()) {
                color = Color.GREEN;
            } else {
                color = Color.GREY;
            }
            drawConnection(canvas, topLeft, topRight, color);
            drawConnection(canvas, topRight, bottomRight, color);
            drawConnection(canvas, bottomRight, bottomLeft, color);
            drawConnection(canvas, bottomLeft, topLeft, color);
        }

        for (Map.Entry<Node, RenderingNode> entry : mapping.entrySet()) {
            Node node = entry.getKey();
            RenderingNode rNode = entry.getValue();

            for (Node next : getRenderGraph().getOutgoingNodesForNode(node)) {
                Vector2f first = new Vector2f(rNode.getPosition()).add(rNode.getSize().x, rNode.getSize().y / 2);
                RenderingNode secondWidget = mapping.get(next);

                Vector2f connectionPlace = new Vector2f(secondWidget.getPosition()).addY(secondWidget.getSize().y / 2);
                Vector2f pos1 = new Vector2f(first);
                Vector2f pos2 = new Vector2f(connectionPlace);
                drawConnection(canvas, pos1, pos2, Color.WHITE);
            }
        }

    }

    public void setRenderGraph(RenderGraph renderGraph) {
        this.renderGraph = renderGraph;
        removeAllWidgets();
        for (Node node : getRenderGraph().getStartingNodes()) {
            handleNode(node);
        }
        for (RenderingNode firstWidget : castAll(getWidgetsByNode(getRenderGraph().getNodesInTopologicalOrder()))) {
            for (RenderingNode secondWidget :
                    castAll(getWidgetsByNode(getRenderGraph().getOutgoingNodesForNode(firstWidget.getNode())))) {
                if (secondWidget.getPosition().y <= firstWidget.getPosition().getY()) {
                    secondWidget.getPosition().setX(firstWidget.getPosition().x + firstWidget.getSize().x + 2f);
                }
            }
        }
        for (RenderingNode firstWidget : castAll(getWidgets())) {
            for (RenderingNode secondWidget : castAll(getWidgets())) {
                if (secondWidget == firstWidget) {
                    continue;
                }
                if (firstWidget.getPosition().equals(secondWidget.getPosition())) {
                    secondWidget.getPosition().setY(firstWidget.getPosition().y + firstWidget.getSize().y + 2f);
                }
            }
        }
    }

    private Collection<PositionalWidget> getWidgetsByNode(Collection<Node> outgoingNodesForNode) {
        return outgoingNodesForNode.stream().map(mapping::get).collect(Collectors.toList());
    }

    private Iterable<RenderingNode> castAll(Collection<PositionalWidget> widgets) {
        return widgets.stream().map((widget) -> (RenderingNode) widget).collect(Collectors.toList());
    }


    private void handleNode(Node node) {
        if (mapping.containsKey(node)) {
            return;
        }
        RenderingNode widget = new RenderingNode(node, new Vector2f(0,
                0),
                new Vector2f(nodeWidth, nodeHeight));
        mapping.put(node, widget);
        for (Node child : getRenderGraph().getOutgoingNodesForNode(node)) {
            handleNode(child);
        }
        addWidget(widget);
    }


    private void drawConnection(Canvas canvas, Vector2f from, Vector2f to, Color color) {
        Vector2i s = worldToScreen(from);
        Vector2i e = worldToScreen(to);
        canvas.drawLine(s.x, s.y, e.x, e.y, color);

    }

    private class RenderingNode extends CoreWidget implements PositionalWidget<ZoomableLayout> {

        private final Node node;
        private Vector2f position;
        private Vector2f size;

        public RenderingNode(Node node, Vector2f pos, Vector2f size) {
            this.node = node;
            position = pos;
            this.size = size;
        }

        @Override
        public Vector2f getPosition() {
            return position;
        }

        @Override
        public Vector2f getSize() {
            return size;
        }

        public Node getNode() {
            return node;
        }

        @Override
        public void onAdded(ZoomableLayout layout) {
        }

        @Override
        public void onRemoved(ZoomableLayout layout) {
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawText(node.getUri().toString());
        }

        @Override
        public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
            return worldToScreen(new Vector2f(nodeWidth, nodeHeight));
        }
    }
}
