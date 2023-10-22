// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.ui;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.math.DoubleMath;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.config.flexible.bindings.MappingBinding;
import org.terasology.engine.config.flexible.bindings.SettingBinding;
import org.terasology.engine.config.flexible.constraints.ColorConstraint;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureUtil;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.CieCamColors;
import org.terasology.nui.Color;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UISlider;

import java.math.RoundingMode;
import java.util.List;

public class ColorConstraintWidgetFactory extends AssetBackedConstraintWidgetFactory<Color, ColorConstraint> {

    @In
    private AssetManager assetManager;

    private final List<Color> colors = CieCamColors.L65C65;

    public ColorConstraintWidgetFactory() {
        super("engine:colorPickerWidget");
    }

    @Override
    protected void bindWidgetToSetting(UIWidget widget) {
        UIImage img = widget.find("image", UIImage.class);
        if (img != null) {
            ResourceUrn uri = TextureUtil.getTextureUriForColor(Color.WHITE);
            Texture tex = assetManager.getAsset(uri, Texture.class).get();
            img.setImage(tex);
            img.bindTint(new SettingBinding<>(getSetting()));
        }

        UISlider slider = widget.find("tone", UISlider.class);
        slider.setIncrement(0.01f);
        Function<Object, String> constant = Functions.constant("  ");   // ensure a certain width
        slider.setLabelFunction(constant);

        slider.bindValue(
                new MappingBinding<>(
                        new SettingBinding<>(getSetting()),
                        this::findClosestColor,
                        this::findClosestIndex
                ));
    }

    private float findClosestIndex(Color color) {
        int best = 0;
        float minDist = Float.MAX_VALUE;
        for (int i = 0; i < colors.size(); i++) {
            Color other = colors.get(i);
            float dr = other.rf() - color.rf();
            float dg = other.gf() - color.gf();
            float db = other.bf() - color.bf();

            // there are certainly smarter ways to measure color distance,
            // but Euclidean distance is good enough for the purpose
            float dist = dr * dr + dg * dg + db * db;
            if (dist < minDist) {
                minDist = dist;
                best = i;
            }
        }

        float max = colors.size() - 1;
        return best / max;
    }

    private Color findClosestColor(float findex) {
        int index = DoubleMath.roundToInt(findex * (double) (colors.size() - 1), RoundingMode.HALF_UP);
        return colors.get(index);
    }
}
