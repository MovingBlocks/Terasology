/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.shader;

import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.properties.IPropertyProvider;
import org.terasology.properties.Property;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.util.List;

/**
 * Basic shader parameters for all shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersSky extends ShaderParametersBase {

    Property colorExp = new Property("colorExp", 12.0f, 0.0f, 100.0f);

    public ShaderParametersSky() {
    }

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);
        program.setFloat("colorExp", (Float) colorExp.getValue());
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(colorExp);
    }
}
