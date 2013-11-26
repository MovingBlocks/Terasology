/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.propagation.light;

import org.terasology.math.Vector3i;
import org.terasology.world.ChunkViewCore;
import org.terasology.world.propagation.AbstractChunkView;

/**
 * @author Immortius
 */
public class SunlightChunkView extends AbstractChunkView {

    public SunlightChunkView(ChunkViewCore chunkView) {
        super(chunkView);
    }

    @Override
    protected byte getValueAt(ChunkViewCore view, Vector3i pos) {
        return view.getSunlight(pos);
    }

    @Override
    protected void setValueAt(ChunkViewCore view, Vector3i pos, byte value) {
        view.setSunlight(pos, value);
    }
}
