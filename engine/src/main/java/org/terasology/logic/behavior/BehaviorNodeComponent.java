/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.behavior;

import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.Color;

/**
 * Defines a renderable node used to display behavior trees.
 *
 */
@API
public class BehaviorNodeComponent implements Component {
    public static final BehaviorNodeComponent DEFAULT = new BehaviorNodeComponent();

    public String type;
    public String name;
    public String category;                     // for palette
    public String shape = "diamond";            // diamond or rect
    public Color color = Color.GREY;
    public Color textColor = Color.BLACK;
    public String description = "";

    @Override
    public String toString() {
        return name;
    }
}
