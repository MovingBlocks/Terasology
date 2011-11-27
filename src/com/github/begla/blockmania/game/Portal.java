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

package com.github.begla.blockmania.game;

import javax.vecmath.Vector3f;

/**
 * Portals are a key structure component of the world
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class Portal {

    private Vector3f _blockLocation;

    public Portal(Vector3f loc) {
        _blockLocation = loc;
    }

    public Vector3f getBlockLocation() {
        return _blockLocation;
    }


}
