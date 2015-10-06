/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.math.DoubleMath;
import org.terasology.asset.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UIText;

import java.math.RoundingMode;
import java.util.List;

/**
 * @author Martin Steiger
 */
public class PlayerSettingsScreen extends CoreScreenLayer {

    @In
    private Config config;

    private final List<Color> colors = CieCamColors.L65C65;

    private UIText nametext;
    private UISlider slider;
    private UIImage img;

    @Override
    public void onOpened() {
        super.onOpened();
        if (nametext != null) {
            nametext.setText(config.getPlayer().getName());
        }
        if (slider != null) {
            Color color = config.getPlayer().getColor();
            slider.bindValue(new NotifyingBinding(findClosestIndex(color)));
        }
        updateImage();
    }

    @Override
    public void initialise() {
        nametext = find("playername", UIText.class);
        img = find("image", UIImage.class);
        slider = find("tone", UISlider.class);
        if (slider != null) {
            slider.setIncrement(0.01f);
            Function<Object, String> constant = Functions.constant("  ");   // ensure a certain width
            slider.setLabelFunction(constant);
        }

        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });

        WidgetUtil.trySubscribe(this, "ok", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                savePlayerSettings();
                getManager().popScreen();
            }
        });
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
        int index = DoubleMath.roundToInt(findex * (colors.size() - 1), RoundingMode.HALF_UP);
        Color color = colors.get(index);
        return color;
    }

    private void updateImage() {
        Color color = getColor();
        ResourceUrn uri = TextureUtil.getTextureUriForColor(color);
        Texture tex = Assets.get(uri, Texture.class).get();
        if (img != null) {
            img.setImage(tex);
        }
    }

    private Color getColor() {
        if (slider != null) {
            float index = slider.getValue();
            return findClosestColor(index);
        } else {
            return config.getPlayer().getColor();
        }
    }

    private void savePlayerSettings() {
        Color color = getColor();
        config.getPlayer().setColor(color);
        if (nametext != null) {
            config.getPlayer().setName(nametext.getText());
        }
    }

    /**
     * Calls update() in parent class when the slider value changes
     */
    private final class NotifyingBinding extends DefaultBinding<Float> {

        private NotifyingBinding(Float value) {
            super(value);
        }

        @Override
        public void set(Float v) {
            super.set(v);

            updateImage();
        }
    }
}
