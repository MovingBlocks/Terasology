/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.animation;

/*
 * Base linear interpolator, should typically be extended from.
 */
public class BaseInterpolator implements Interpolator {
    protected float start;
    protected float end;

    public BaseInterpolator() {
        start = 0;
        end = 1;
    }

    public float getInterpolation(float v) {
        return v * (end - start) + start;
    }

    public void setStart(float val) {
        this.start = val;
    }

    public float getStart() {
        return start;
    }

    public void setEnd(float val) {
        this.end = val;
    }

    public float getEnd() {
        return end;
    }
}
