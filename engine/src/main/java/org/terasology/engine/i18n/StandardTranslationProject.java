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

package org.terasology.i18n;

import org.terasology.i18n.assets.Translation;
import org.terasology.naming.Name;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Performs textual translations based on a set of {@link Translation} instances.
 */
public class StandardTranslationProject implements TranslationProject {

    private final Map<Locale, Translation> translations = new HashMap<>();

    @Override
    public void add(Translation trans) {
        translations.put(trans.getLocale(), trans);
    }

    @Override
    public void remove(Translation trans) {
        translations.remove(trans.getLocale(), trans);
    }

    @Override
    public Optional<String> translate(Name key, Locale locale) {
        String result = translateExact(key, locale);
        if (result == null && !locale.getVariant().isEmpty()) {
            // try without variant
            Locale fallbackLocale = new Locale(locale.getLanguage(), locale.getCountry());
            result = translateExact(key, fallbackLocale);
        }
        if (result == null && !locale.getCountry().isEmpty()) {
            // try without country
            Locale fallbackLocale = new Locale(locale.getLanguage());
            result = translateExact(key, fallbackLocale);
        }
        if (result == null) {
            result = translateExact(key, Locale.ENGLISH);
        }

        return Optional.ofNullable(result);
    }

    @Override
    public Set<Locale> getAvailableLocales() {
        Set<Locale> result = new HashSet<>(translations.keySet());
        result.remove(Locale.ROOT);
        return result;
    }

    private String translateExact(Name key, Locale locale) {
        Translation trans = translations.get(locale);
        if (trans != null) {
            return trans.lookup(key);
        }
        return null;
    }
}
