// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.testScreens;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;

public class MathTypeWidgetTestScreen extends TypeWidgetTestScreen {
    @Override
    protected void addWidgets() {
        newBinding(Vector2f.class);
        newBinding(Vector2i.class);
        newBinding(Vector3f.class);
        newBinding(Vector3i.class);
        newBinding(Vector4f.class);

        newBinding(Quaternionf.class);

        newBinding(Rectanglei.class);
        newBinding(Rectanglef.class);
    }
}
