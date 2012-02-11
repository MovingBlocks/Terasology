/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.model.structures;

import org.terasology.game.Terasology;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersBlock extends ShaderParameters {

    public ShaderParametersBlock() {
        super("block");
    }

    public void applyParameters() {
        Terasology tera = Terasology.getInstance();

        setFloat("light", tera.getActiveWorldRenderer().getRenderingLightValue());
        setInt("carryingTorch", tera.getActivePlayer().isCarryingTorch() ? 1 : 0);
        setFloat3("colorOffset", 1.0f, 1.0f, 1.0f);
        setInt("textured", 1);
    }

}
