/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.widgets.browser.data.basic.flow;

import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public final class FlowLineBuilder {
    private FlowLineBuilder() {
    }

    public static <T extends FlowRenderable<T>> Iterable<LaidFlowLine<T>> getLines(Collection<T> flowRenderables, TextRenderStyle defaultRenderStyle,
                                                                                   int yStart, ContainerRenderSpace containerRenderSpace) {
        // Take into account a minimum width
        int minWidth = determineMinWidth(flowRenderables, defaultRenderStyle);

        int x = 0;
        int y = yStart;

        int availableWidth = Math.max(minWidth, containerRenderSpace.getWidthForVerticalPosition(y));

        List<LaidFlowLine<T>> result = new LinkedList<>();

        int maxHeightInLine = 0;

        Deque<T> renderablesQueue = new LinkedList<>(flowRenderables);
        List<T> renderablesInLine = new LinkedList<>();
        while (!renderablesQueue.isEmpty()) {
            FlowRenderable<T> flowRenderable = renderablesQueue.removeFirst();
            FlowRenderable.SplitResult<T> splitResult = flowRenderable.splitAt(defaultRenderStyle, availableWidth - x);
            if (splitResult.before == null) {
                // This is the end of the line, this renderable doesn't fit
                result.add(new DefaultLaidFlowLine<>(x, maxHeightInLine, renderablesInLine));
                renderablesInLine = new LinkedList<>();
                x = 0;
                y += maxHeightInLine;
                availableWidth = Math.max(minWidth, containerRenderSpace.getWidthForVerticalPosition(y));
                maxHeightInLine = 0;
                renderablesQueue.addFirst(splitResult.rest);
            } else {
                // Append the "before" part and push the "rest" onto the Deque if not null
                int renderableWidth = splitResult.before.getWidth(defaultRenderStyle);
                int renderableHeight = splitResult.before.getHeight(defaultRenderStyle);

                x += renderableWidth;
                maxHeightInLine = Math.max(maxHeightInLine, renderableHeight);
                renderablesInLine.add(splitResult.before);

                if (splitResult.rest != null) {
                    result.add(new DefaultLaidFlowLine<>(x, maxHeightInLine, renderablesInLine));
                    renderablesInLine = new LinkedList<>();
                    x = 0;
                    y += maxHeightInLine;
                    availableWidth = Math.max(minWidth, containerRenderSpace.getWidthForVerticalPosition(y));
                    maxHeightInLine = 0;
                    renderablesQueue.addFirst(splitResult.rest);
                }
            }
        }

        result.add(new DefaultLaidFlowLine<>(x, maxHeightInLine, renderablesInLine));

        return result;
    }

    private static <T extends FlowRenderable> int determineMinWidth(Iterable<T> flowRenderables, TextRenderStyle defaultRenderStyle) {
        int minWidth = 0;
        for (FlowRenderable flowRenderable : flowRenderables) {
            minWidth = Math.max(minWidth, flowRenderable.getMinWidth(defaultRenderStyle));
        }
        return minWidth;
    }

}
