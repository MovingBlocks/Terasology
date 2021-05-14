// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n;

import org.terasology.engine.i18n.assets.Translation;
import org.terasology.gestalt.naming.Name;

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
