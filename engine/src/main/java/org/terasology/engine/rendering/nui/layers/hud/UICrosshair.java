// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.hud;

import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.terasology.math.TeraMath;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;

import java.util.List;

/**
 */
public class UICrosshair extends CoreWidget {

    @LayoutConfig
    private List<UITextureRegion> chargeStages = Lists.newArrayList();

    private Binding<Float> chargeAmount = new DefaultBinding<>(0f);

    @Override
    public void onDraw(Canvas canvas) {
        if (getChargeAmount() > 0 && !chargeStages.isEmpty()) {
            int state = TeraMath.floorToInt(getChargeAmount() * (chargeStages.size() - 1));
            canvas.drawTexture(chargeStages.get(state));
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i();
    }

    public void bindChargeAmount(Binding<Float> binding) {
        chargeAmount = binding;
    }

    public float getChargeAmount() {
        return Math.min(1.0f, chargeAmount.get());
    }

    public void setChargeAmount(float val) {
        chargeAmount.set(val);
    }
}
