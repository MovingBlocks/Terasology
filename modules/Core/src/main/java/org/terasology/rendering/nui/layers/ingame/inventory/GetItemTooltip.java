/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.inventory;

import org.terasology.entitySystem.event.Event;
import org.terasology.rendering.nui.widgets.TooltipLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetItemTooltip implements Event {
    private List<TooltipLine> tooltipLines;

    public GetItemTooltip() {
        tooltipLines = new ArrayList<>();
    }

    public GetItemTooltip(String defaultTooltip) {
        this.tooltipLines = new ArrayList<>(Arrays.asList(new TooltipLine(defaultTooltip)));
    }

    public List<TooltipLine> getTooltipLines() {
        return tooltipLines;
    }
}
