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

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Helper {

    private double timeLastFrame;
    private double timeLastUpdate;
    private int frameCounter;
    private static Helper instance = null;

    public static Helper getInstance() {
        if (instance == null) {
            instance = new Helper();
        }

        return instance;
    }

    public Helper() {
        updateFPSDisplay();
    }

    public double getTime() {
        return (Sys.getTime() * 1000d) / Sys.getTimerResolution();
    }

    public void frameRendered() {
        frameCounter++;
        timeLastFrame = getTime();

        if (getTime() - timeLastUpdate > 1000) {
            calculateFPS();
            updateFPSDisplay();
        }
    }

    public double calcInterpolation() {
        double fps = calculateFPS();

        if (fps > 0d) {
            return 60d / fps;
        }

        return 0d;
    }

    public double timeSinceLastFrame() {
        return getTime() - timeLastFrame;
    }

    private double calculateFPS() {
        double secondsSinceLastUpdate = ((getTime() - timeLastUpdate) / 1000d);
        if (secondsSinceLastUpdate > 0d) {
            return frameCounter / secondsSinceLastUpdate;
        }
        return 0d;
    }

    private void updateFPSDisplay() {
        Display.setTitle("Blockmania (FPS: " + Math.round(calculateFPS()) + ", Quads: " + Chunk.quadCounter + ", Interpolation: " + calcInterpolation() + ")");

        timeLastUpdate = getTime();
        frameCounter = 0;
    }
}
