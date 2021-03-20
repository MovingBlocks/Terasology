// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.mainMenu.settings;

import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.itemRendering.StringTextIconRenderer;
import org.terasology.engine.utilities.Assets;

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
