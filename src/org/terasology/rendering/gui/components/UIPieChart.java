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
package org.terasology.rendering.gui.components;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * Colored pie chart.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIPieChart extends UIDisplayContainer {

    private static final int SEGMENTS = 32;
    private HashMap<String, Double> _data = new HashMap<String, Double>();

    public UIPieChart() {
        super();

        setSize(new Vector2f(256f, 256f));
    }

    public void render() {
        super.render();

        if (_data.size() == 0)
            return;

        ArrayList<String> sortedKeys = new ArrayList<String>(_data.keySet());
        Collections.sort(sortedKeys);

        glPushMatrix();
        glTranslatef(getSize().x / 2, getSize().y / 2, 0.0f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glVertex3f(0f, 0f, 0f);

        double stepSize = (Math.PI * 2.0) / SEGMENTS;

        int segment = 0;
        double prevValueSum = 0.0f;

        for (int i = 0; i <= SEGMENTS; i++) {
            double value = _data.get(sortedKeys.get(segment));

            if (((double) i / SEGMENTS) >= value + prevValueSum) {
                segment++;
                prevValueSum += value;

                Vector3f segmentColor = new Vector3f((segment * 0.1f) % 1.0f, (segment * 0.2f) % 1.0f, (segment * 0.4f) % 1.0f);
                glColor3f(segmentColor.x, segmentColor.y, segmentColor.z);
            }

            GL11.glVertex2f((float) Math.sin(stepSize * i) * (getSize().x / 2), (float) Math.cos(stepSize * i) * (getSize().x / 2));
        }

        GL11.glEnd();
        glPopMatrix();
    }

    @Override
    public void update() {
        super.update();
    }

    public void setData(HashMap<String, Double> data) {
        if (data != null) {
            _data = data;
        }
    }
}
