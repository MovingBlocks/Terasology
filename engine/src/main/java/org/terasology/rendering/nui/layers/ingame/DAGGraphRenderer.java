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

import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.dependencyConnections.BufferPairConnection;
import org.terasology.rendering.dag.dependencyConnections.DependencyConnection;
import org.terasology.rendering.dag.dependencyConnections.FboConnection;
import org.terasology.rendering.dag.dependencyConnections.RunOrderConnection;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.layouts.ZoomableLayout;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DAG Renderer.
 * Render DAG nodes on screen with edge connections.
 */
public class DAGGraphRenderer extends ZoomableLayout {

    private RenderGraph renderGraph;
    private RenderNodeWidget selected;
    private final Map<Node, RenderNodeWidget> mapping = new HashMap<>();
    private final float nodeWidth = 20f;
    private final float nodeHeight = 3f;

    private static RenderGraph.ConnectionType getConnectionType(DependencyConnection connection) {
        RenderGraph.ConnectionType connectionType;
        if (connection instanceof RunOrderConnection) {
            connectionType = RenderGraph.ConnectionType.RUN_ORDER;
        } else if (connection instanceof FboConnection) {
            connectionType = RenderGraph.ConnectionType.FBO;
        } else if (connection instanceof BufferPairConnection) {
            connectionType = RenderGraph.ConnectionType.BUFFER_PAIR;
        } else {
            throw new IllegalStateException("Detected unknown DAG's connection type!");
        }
        return connectionType;
    }

    public RenderGraph getRenderGraph() {
        return renderGraph;
    }

    public void setRenderGraph(RenderGraph renderGraph) {
        this.renderGraph = renderGraph;
        removeAllWidgets();
        for (Node node : getRenderGraph().getStartingNodes()) {
            handleNode(node);
        }
        locateRenderNodeWidgets();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RenderNodeWidget widget : castAll(getWidgets())) {
            Vector2f size = widget.getSize();
            Vector2f position = widget.getPosition();
            drawWidgetBorder(canvas, widget, size, position);

            for (DAGConnection connection : getOutgoingNodesForNode(widget.getNode())) {
                drawConnection(canvas, widget, connection);
            }
        }

    }

    private void drawConnection(Canvas canvas, RenderNodeWidget widget, DAGConnection connection) {
        Vector2f first = new Vector2f(widget.getPosition()).add(widget.getSize().x, widget.getSize().y / 2);
        RenderNodeWidget secondWidget = mapping.get(connection.getNode());
        Vector2f connectionPlace = new Vector2f(secondWidget.getPosition()).addY(secondWidget.getSize().y / 2);
        Vector2f pos1 = new Vector2f(first);
        Vector2f pos2 = new Vector2f(connectionPlace);

        Color color;
        switch (connection.getConnectionType()) {
            case FBO:
                color = Color.MAGENTA;
                break;
            case BUFFER_PAIR:
                color = Color.CYAN;
                break;
            case RUN_ORDER:
                color = Color.YELLOW;
                break;
            default:
                color = Color.WHITE;
        }

        if (widget == selected) {
            canvas.setDrawOnTop(true);
            drawConnection(canvas, pos1, pos2, color.alterRed(5));
            canvas.setDrawOnTop(false);
        } else {
            drawConnection(canvas, pos1, pos2, color);
        }
    }

    private void drawWidgetBorder(Canvas canvas, RenderNodeWidget widget, Vector2f size, Vector2f topLeft) {
        Vector2f topRight = new Vector2f(topLeft).addX(size.x);
        Vector2f bottomLeft = new Vector2f(topLeft).addY(size.y);
        Vector2f bottomRight = new Vector2f(topLeft).add(new Vector2f(size.x, size.y));

        Color color;
        if (widget.getNode().isEnabled()) {
            color = Color.GREEN;
        } else {
            color = Color.GREY;
        }
        if (widget == selected) {
            color = color.alterBlue(30);
        }
        drawConnection(canvas, topLeft, topRight, color);
        drawConnection(canvas, topRight, bottomRight, color);
        drawConnection(canvas, bottomRight, bottomLeft, color);
        drawConnection(canvas, bottomLeft, topLeft, color);
    }

    private Set<DAGConnection> getOutgoingNodesForNode(Node node) {
        Set<DAGConnection> connections = new HashSet<>();
        Collection<DependencyConnection> outputDependencyConnections = node.getOutputConnections().values();
        for (DependencyConnection outputDependencyConnection : outputDependencyConnections) {
            for (DependencyConnection inputOfNextNode :
                    (Collection<DependencyConnection>) outputDependencyConnection.getConnectedConnections().values()) {
                connections.add(new DAGConnection(inputOfNextNode));
            }
        }
        return connections;
    }


    //TODO: Use normal graph rendering method

    /**
     * Find and place NodeWidgets in workPlace
     */
    private void locateRenderNodeWidgets() {
        for (RenderNodeWidget firstWidget :
                castAll(getWidgetsByNode(getRenderGraph().getNodesInTopologicalOrder().stream()))) {
            for (RenderNodeWidget secondWidget :
                    castAll(getWidgetsByNode(getOutgoingNodesForNode(firstWidget.getNode())))) {
                if (secondWidget.getPosition().y <= firstWidget.getPosition().getY()) {
                    secondWidget.getPosition().setX(firstWidget.getPosition().x + firstWidget.getSize().x + 2f);
                }
            }
        }
        for (RenderNodeWidget firstWidget : castAll(getWidgets())) {
            for (RenderNodeWidget secondWidget : castAll(getWidgets())) {
                if (secondWidget == firstWidget) {
                    continue;
                }
                if (firstWidget.getPosition().equals(secondWidget.getPosition())) {
                    secondWidget.getPosition().setY(firstWidget.getPosition().y + firstWidget.getSize().y + 2f);
                }
            }
        }
    }

    private Collection<PositionalWidget> getWidgetsByNode(Stream<Node> nodeStream) {
        return nodeStream
                .map(mapping::get)
                .collect(Collectors.toList());
    }

    private Collection<PositionalWidget> getWidgetsByNode(Collection<DAGConnection> connections) {
        return getWidgetsByNode(connections.stream()
                .map(DAGConnection::getNode));
    }

    private Iterable<RenderNodeWidget> castAll(Collection<PositionalWidget> widgets) {
        return widgets.stream().map((widget) -> (RenderNodeWidget) widget).collect(Collectors.toList());
    }

    private void handleNode(Node node) {
        if (mapping.containsKey(node)) {
            return;
        }
        RenderNodeWidget widget = new RenderNodeWidget(node, Vector2f.zero(), new Vector2f(nodeWidth, nodeHeight));
        mapping.put(node, widget);
        for (DAGConnection child : getOutgoingNodesForNode(node)) {
            handleNode(child.getNode());
        }
        addWidget(widget);
    }

    private void drawConnection(Canvas canvas, Vector2f from, Vector2f to, Color color) {
        Vector2i s = worldToScreen(from);
        Vector2i e = worldToScreen(to);
        canvas.drawLine(s.x, s.y, e.x, e.y, color);

    }

    private class RenderNodeWidget extends CoreWidget implements PositionalWidget<ZoomableLayout> {

        private final Node node;
        private Vector2f position;
        private Vector2f size;

        public RenderNodeWidget(Node node, Vector2f pos, Vector2f size) {
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
            canvas.addInteractionRegion(new BaseInteractionListener() {
                @Override
                public void onMouseOver(NUIMouseOverEvent event) {
                    selected = RenderNodeWidget.this;
                }

            }, node.toString());
            canvas.drawText(node.getUri().toString());
        }

        @Override
        public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
            return worldToScreen(new Vector2f(nodeWidth, nodeHeight));
        }
    }

    /**
     * Pair of ConnectionType and Node representation.
     */
    private class DAGConnection {
        private final RenderGraph.ConnectionType connectionType;
        private final Node node;

        public DAGConnection(RenderGraph.ConnectionType connectionType, Node nextNode) {
            this.connectionType = connectionType;
            this.node = nextNode;
        }

        public DAGConnection(DependencyConnection connection) {
            this(DAGGraphRenderer.getConnectionType(connection), getRenderGraph().findNode(connection.getParentNode()));
        }


        public RenderGraph.ConnectionType getConnectionType() {
            return connectionType;
        }

        public Node getNode() {
            return node;
        }
    }
}
