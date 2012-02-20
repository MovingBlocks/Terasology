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
package org.terasology.math;


import javax.vecmath.Matrix4f;
import java.nio.FloatBuffer;

/**
 * An extended version of the VecMath Matrix4f providing support for various
 * OpenGL related stuff.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TeraMatrix4f extends Matrix4f {

    public FloatBuffer toOpenGLMatrix() {
          return null;
    }

}
