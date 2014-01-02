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
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseLayouts.ZoomableLayout;

import javax.vecmath.Vector2f;

/**
 * Created by synopia on 02.01.14.
 */
public class BehaviorEditor extends ZoomableLayout {
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
        try (SubRegion subRegion = canvas.subRegion(canvas.getRegion(), false)) {
            canvas.setDrawOnTop(true);
            for (UIWidget widget : getWidgets()) {
                if (widget instanceof RenderableNode) {
                    RenderableNode renderableNode = (RenderableNode) widget;
                    for (Port port : renderableNode.getPorts()) {
                        Vector2f start = new Vector2f(renderableNode.getPosition());
                        start.add(port.mid());

                        Port targetPort = port.getTargetPort();
                        if (port.isInput() || targetPort == null) {
                            continue;
                        }
                        Vector2f end = new Vector2f(targetPort.getSourceNode().getPosition());
                        end.add(targetPort.mid());
                        Vector2i s = worldToScreen(start);
                        Vector2i e = worldToScreen(end);
                        canvas.drawLine(s.x, s.y, e.x, e.y, Color.WHITE);
                    }
                }
            }
            canvas.setDrawOnTop(false);
        }
    }
}
