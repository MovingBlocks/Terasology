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
package org.terasology.logic.nameTags;

import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.Color;

/**
 * Will make the entity have a name tag overhead in the 3D view.
 *
 * The text on name tag is based on the {@link org.terasology.logic.common.DisplayNameComponent} this entity.
 *
 * The color of the name tag is based on the {@link org.terasology.network.ColorComponent} of this entity
 */
@API
public class NameTagComponent implements Component {

    public float yOffset = 0.3f;

    public String text;

    public Color textColor = Color.WHITE;

    public float scale = 1f;
}
