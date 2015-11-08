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

import java.util.Locale;

import org.terasology.i18n.TranslationSystem;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;

/**
 * Renders locale in the format "{@literal <native name> (<English name>)}".
 */
public class LocaleRenderer extends StringTextRenderer<Locale> {

    private final TranslationSystem translationSystem;

    /**
     * @param translationSystem the translation system to use for locale formatting
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


}
