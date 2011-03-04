/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package blockmania;

import org.lwjgl.util.vector.Vector3f;


/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class RenderObject {

    protected Vector3f position = new Vector3f(0.0f,0.0f,0.0f);

    public void render() {
    }

    public final Vector3f getPosition() {
        return position;
    }

    public final void setPosition(Vector3f position) {
        this.position = position;
    }

}
