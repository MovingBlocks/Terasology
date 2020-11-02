// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.i18n;

import org.terasology.i18n.assets.Translation;
import org.terasology.naming.Name;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Describes a translation project. It aggregates individual translations (one per {@link Locale}).
 */
public interface TranslationProject {

    /**
     * Adds a translation asset. If an assets for a given locale existed before it will be replaced with the new entry.
     * @param trans the translation to add.
     */
    void add(Translation trans);

    /**
     * Removes a translation asset.
     * @param trans the translation to remove. Non-existing entries are ignored.
     */
    void remove(Translation trans);

    /**
     * If no perfect match is found for the given locale, fallback strategies will attempt to find the closest match.
     * @param id the id of the string to translate (without project reference).
     * @param locale the target locale
     * @param arguments {@link java.text.MessageFormat} arguments for the translation string
     * @return the translated string
     */
    Optional<String> translate(Name id, Locale locale, Object... arguments);

    /**
     * @return the set of registered locales with at least one entry
     */
    Set<Locale> getAvailableLocales();
}
