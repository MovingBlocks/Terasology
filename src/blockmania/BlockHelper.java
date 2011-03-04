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

import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockHelper {

    public static final float div = 1.0f / 16.0f;

    public static enum SIDE {

        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK;
    };

    static Vector2f calcOffsetForTextureAt(int x, int y) {
        return new Vector2f(x * div, y * div);
    }

    public static Vector2f getTextureOffsetFor(int type, SIDE side) {
        switch (type) {
            case 0x1:
                if (side == SIDE.LEFT || side == SIDE.RIGHT || side == SIDE.FRONT || side == SIDE.BACK) {
                    return calcOffsetForTextureAt(3, 0);
                } else if (side == SIDE.TOP) {
                    calcOffsetForTextureAt(0, 0);
                } else {
                    calcOffsetForTextureAt(2, 0);
                }
                break;
            case 0x2:
                calcOffsetForTextureAt(2, 0);
                break;
        }

        return calcOffsetForTextureAt(2, 0);
    }
}
