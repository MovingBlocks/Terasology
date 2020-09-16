/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.i18n.TranslationSystem;
import org.terasology.nui.UITextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.nui.itemRendering.StringTextIconRenderer;
import org.terasology.utilities.Assets;

import java.util.Locale;
import java.util.Optional;

/**
 * Renders locale in the format "{@literal <native name> (<English name>)}" along with a flag icon.
 */
public class LocaleRenderer extends StringTextIconRenderer<Locale> {
    private static final String ICON_BLANK = "engine:icon_blank";

    private final TranslationSystem translationSystem;

    /**
     * @param translationSystem The translation system to be used for locale formatting.
     */
    public LocaleRenderer(TranslationSystem translationSystem) {
        this.translationSystem = translationSystem;
    }

    @Override
    public String getString(Locale locale) {
        String nat = translationSystem.translate("${engine:menu#this-language-native}", locale);
        String eng = translationSystem.translate("${engine:menu#this-language-English}", locale);
        return String.format("%s (%s)", nat, eng);
    }

    @Override
    public UITextureRegion getTexture(Locale value) {
        Optional<Texture> texture = Assets.getTexture(String.format("engine:flag_%s", value.getLanguage()));
        if (texture.isPresent()) {
            return (UITextureRegion) texture.get();
        } else {
            return (UITextureRegion) Assets.getTexture(ICON_BLANK).get();
        }
    }
}
