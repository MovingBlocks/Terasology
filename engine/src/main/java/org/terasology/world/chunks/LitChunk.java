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
package org.terasology.world.chunks;

import org.terasology.math.Vector3i;
import org.terasology.module.sandbox.API;

/**
 * @author Immortius
 */
@API
public interface LitChunk extends CoreChunk {

    byte getSunlight(Vector3i pos);

    byte getSunlight(int x, int y, int z);

    boolean setSunlight(Vector3i pos, byte amount);

    boolean setSunlight(int x, int y, int z, byte amount);

    byte getSunlightRegen(Vector3i pos);

    byte getSunlightRegen(int x, int y, int z);

    boolean setSunlightRegen(Vector3i pos, byte amount);

    boolean setSunlightRegen(int x, int y, int z, byte amount);

    byte getLight(Vector3i pos);

    byte getLight(int x, int y, int z);

    boolean setLight(Vector3i pos, byte amount);

    boolean setLight(int x, int y, int z, byte amount);
}
