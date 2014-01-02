/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.nui;

import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseLayouts.ZoomableLayout;

import javax.vecmath.Vector2f;

/**
 * Created by synopia on 02.01.14.
 */
public class BehaviorEditor extends ZoomableLayout {
    private Port activeConnectionStart;
    private Vector2f mousePos;

    private final InteractionListener moveOver = new BaseInteractionListener(){


        @Override
        public void onMouseOver(Vector2i pos, boolean topMostElement) {
            mousePos = screenToWorld(pos);
        }
    };

    public BehaviorEditor() {
    }

    public BehaviorEditor(String id) {
        super(id);
    }

    public void setTree(BehaviorTree tree) {
        removeAll();
        for (RenderableNode widget : tree.getRenderableNodes()) {
            addWidget(widget);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.addInteractionRegion(moveOver);
        try (SubRegion subRegion = canvas.subRegion(canvas.getRegion(), false)) {
            canvas.setDrawOnTop(true);
            for (UIWidget widget : getWidgets()) {
                if (widget instanceof RenderableNode) {
                    RenderableNode renderableNode = (RenderableNode) widget;
                    for (Port port : renderableNode.getPorts()) {
                        Port targetPort = port.getTargetPort();
                        if (port.isInput() || targetPort == null ) {
                            continue;
                        }
                        drawConnection(canvas, port, targetPort, port==activeConnectionStart ? Color.BLACK : Color.GREY);
                    }
                }
            }
            if( activeConnectionStart!=null ) {
                drawConnection(canvas, activeConnectionStart, mousePos, Color.WHITE);
            }

            canvas.setDrawOnTop(false);
        }
    }

    public void portClicked(Port port) {
        if( activeConnectionStart==null ) {
            activeConnectionStart = port;
        } else {
            if( activeConnectionStart.isInput() && !port.isInput() ) {
                ((Port.OutputPort)port).setTarget((Port.InputPort) activeConnectionStart);
            } else if( !activeConnectionStart.isInput() && port.isInput() ) {
                ((Port.OutputPort)activeConnectionStart).setTarget((Port.InputPort) port);
            }
            activeConnectionStart = null;
        }
    }

    private void drawConnection( Canvas canvas, Vector2f from, Vector2f to, Color color) {
        Vector2i s = worldToScreen(from);
        Vector2i e = worldToScreen(to);
        canvas.drawLine(s.x, s.y, e.x, e.y, color);

    }
    private void drawConnection( Canvas canvas, Port from, Vector2f to, Color color) {
        Vector2f start = new Vector2f(from.node.getPosition());
        start.add(from.mid());
        drawConnection(canvas, start, to, color);
    }
    private void drawConnection( Canvas canvas, Port from, Port to, Color color) {
        Vector2f start = new Vector2f(from.node.getPosition());
        start.add(from.mid());
        Vector2f end = new Vector2f(to.node.getPosition());
        end.add(to.mid());
        drawConnection(canvas, start, end, color);
    }
}
