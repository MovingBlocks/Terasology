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
package org.terasology.rendering.nui.widgets.browser.ui;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class ContainerFlowContainerRenderSpace implements ContainerRenderSpace {
    private Deque<Rect2i> leftFloats = new LinkedList<>();
    private Deque<Rect2i> rightFloats = new LinkedList<>();
    private int containerWidth;

    public ContainerFlowContainerRenderSpace(int containerWidth) {
        this.containerWidth = containerWidth;
    }

    @Override
    public int getContainerWidth() {
        return containerWidth;
    }

    @Override
    public int getNextWidthChange(int y) {
        Rect2i lastLeftFloat = findLastAtYPosition(leftFloats, y);
        Rect2i lastRightFloat = findLastAtYPosition(rightFloats, y);

        if (lastLeftFloat != null && lastRightFloat != null) {
            return Math.min(lastLeftFloat.maxY(), lastRightFloat.maxY());
        } else if (lastLeftFloat != null) {
            return lastLeftFloat.maxY();
        } else if (lastRightFloat != null) {
            return lastRightFloat.maxY();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public Rect2i addLeftFloat(int y, int width, int height) {
        int posY = y;
        while (true) {
            int availableWidth = getAvailableWidthAt(posY);
            if (availableWidth >= width) {
                int x = 0;
                Rect2i lastLeft = findLastAtYPosition(leftFloats, posY);
                if (lastLeft != null) {
                    x = lastLeft.maxX();
                }
                Rect2i floatRect = Rect2i.createFromMinAndSize(x, posY, width, height);
                leftFloats.add(floatRect);
                return floatRect;
            } else {
                Rect2i lastLeft = findLastAtYPosition(leftFloats, posY);
                Rect2i lastRight = findLastAtYPosition(rightFloats, posY);
                if (lastLeft != null && lastRight != null) {
                    posY = Math.min(lastLeft.maxY(), lastRight.maxY());
                } else if (lastLeft != null) {
                    posY = lastLeft.maxY();
                } else if (lastRight != null) {
                    posY = lastRight.maxY();
                }
            }
        }
    }

    @Override
    public Rect2i addRightFloat(int y, int width, int height) {
        int posY = y;
        while (true) {
            int availableWidth = getAvailableWidthAt(posY);
            if (availableWidth >= width) {
                int x = 0;
                Rect2i lastRight = findLastAtYPosition(rightFloats, posY);
                if (lastRight != null) {
                    x = lastRight.minX();
                }
                Rect2i floatRect = Rect2i.createFromMinAndSize(x - width, posY, width, height);
                rightFloats.add(floatRect);
                return floatRect;
            } else {
                Rect2i lastLeft = findLastAtYPosition(leftFloats, posY);
                Rect2i lastRight = findLastAtYPosition(rightFloats, posY);
                if (lastLeft != null && lastRight != null) {
                    posY = Math.min(lastLeft.maxY(), lastRight.maxY());
                } else if (lastLeft != null) {
                    posY = lastLeft.maxY();
                } else if (lastRight != null) {
                    posY = lastRight.maxY();
                }
            }
        }
    }

    @Override
    public int getNextClearYPosition(ParagraphRenderStyle.ClearStyle clearStyle) {
        int maxY = 0;
        if (clearStyle == ParagraphRenderStyle.ClearStyle.LEFT
                || clearStyle == ParagraphRenderStyle.ClearStyle.BOTH) {
            for (Rect2i leftFloat : leftFloats) {
                maxY = Math.max(maxY, leftFloat.maxY());
            }
        }
        if (clearStyle == ParagraphRenderStyle.ClearStyle.RIGHT
                || clearStyle == ParagraphRenderStyle.ClearStyle.BOTH) {
            for (Rect2i rightFloat : rightFloats) {
                maxY = Math.max(maxY, rightFloat.maxY());
            }
        }
        return maxY;
    }

    @Override
    public int getWidthForVerticalPosition(int y) {
        return getAvailableWidthAt(y);
    }

    @Override
    public int getAdvanceForVerticalPosition(int y) {
        Rect2i lastLeft = findLastAtYPosition(leftFloats, y);
        if (lastLeft != null) {
            return lastLeft.maxX();
        } else {
            return 0;
        }
    }

    private int getAvailableWidthAt(int y) {
        int width = containerWidth;
        Rect2i lastRight = findLastAtYPosition(rightFloats, y);
        if (lastRight != null) {
            width = lastRight.minX();
        }
        Rect2i lastLeft = findLastAtYPosition(leftFloats, y);
        if (lastLeft != null) {
            width -= lastLeft.maxX();
        }
        return width;
    }

    private Rect2i findLastAtYPosition(Deque<Rect2i> floats, int y) {
        Iterator<Rect2i> iterator = floats.descendingIterator();
        while (iterator.hasNext()) {
            Rect2i rect = iterator.next();
            if (rect.minY() <= y && rect.maxY() > y) {
                return rect;
            }
        }
        return null;
    }
}
