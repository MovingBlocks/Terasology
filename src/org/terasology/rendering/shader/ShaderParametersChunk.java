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
package org.terasology.rendering.shader;

import org.terasology.game.Terasology;
import org.terasology.model.blocks.management.BlockManager;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk extends ShaderParameters {

    public ShaderParametersChunk() {
        super("chunk");
    }

    public void applyParameters() {
        Terasology tera = Terasology.getInstance();

        setFloat("daylight", (float) tera.getActiveWorldRenderer().getDaylight());
        setInt("swimming", tera.getActivePlayer().isSwimming() ? 1 : 0);
        setInt("carryingTorch", tera.getActivePlayer().isCarryingTorch() ? 1 : 0);
        setFloat2("grassCoordinate", BlockManager.getInstance().calcCoordinate("Grass"));
        setFloat2("waterCoordinate", BlockManager.getInstance().calcCoordinate("Water"));
        setFloat2("lavaCoordinate", BlockManager.getInstance().calcCoordinate("Lava"));
        setFloat1("wavingCoordinates", BlockManager.getInstance().calcCoordinatesForWavingBlocks());
        setFloat("tick", tera.getActiveWorldRenderer().getTick());
        setFloat("sunPosAngle", (float) tera.getActiveWorldRenderer().getSkysphere().getSunPosAngle());
    }

}
