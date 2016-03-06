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
package org.terasology.rendering.nui.layers.mainMenu.settings;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.i18n.TranslationProject;
import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UIText;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 */
public class PlayerSettingsScreen extends CoreScreenLayer {

    @In
    private Config config;
    @In
    private TranslationSystem translationSystem;

    private final List<Color> colors = CieCamColors.L65C65;

    private UIText nametext;
    private UISlider slider;
    private UIImage img;
    private UIDropdownScrollable<Locale> language;

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
        if (language != null) {
            language.setSelection(config.getSystem().getLocale());
        }
        updateImage();
    }

    @Override
    public void initialise() {
        nametext = find("playername", UIText.class);
        if (nametext != null) {
            nametext.setTooltipDelay(0);
            nametext.bindTooltipString(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return validateScreen();
                }
            });
        }
        img = find("image", UIImage.class);
        slider = find("tone", UISlider.class);
        if (slider != null) {
            slider.setIncrement(0.01f);
            Function<Object, String> constant = Functions.constant("  ");   // ensure a certain width
            slider.setLabelFunction(constant);
        }

        language = find("language", UIDropdownScrollable.class);
        if (language != null) {
            SimpleUri menuUri = new SimpleUri("engine:menu");
            TranslationProject menuProject = translationSystem.getProject(menuUri);
            List<Locale> locales = new ArrayList<>(menuProject.getAvailableLocales());
            language.setOptions(Lists.newArrayList(locales));
            language.setVisibleOptions(5); // Set maximum number of options visible for scrolling
            language.setOptionRenderer(new LocaleRenderer(translationSystem));
        }

        WidgetUtil.trySubscribe(this, "close", button -> getManager().popScreen());

        UIButton okButton = find("ok", UIButton.class);
        if (okButton != null) {
            okButton.subscribe(button -> {
                savePlayerSettings();
                getManager().popScreen();
            });
            okButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return Strings.isNullOrEmpty(validateScreen());
                }
            });
            okButton.setTooltipDelay(0);
            okButton.bindTooltipString(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return validateScreen();
                }
            });
        }
    }

    private String validateScreen() {
        if (nametext != null && Strings.isNullOrEmpty(nametext.getText())) {
            return translationSystem.translate("${engine:menu#missing-name-message}");
        }
        return null;
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
        if (!config.getSystem().getLocale().equals(language.getSelection())) {
            config.getSystem().setLocale(language.getSelection());
            getManager().invalidate();
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
