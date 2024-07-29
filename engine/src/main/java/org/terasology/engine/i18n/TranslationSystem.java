// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.context.annotation.API;
import org.terasology.nui.translate.Translator;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * A translation system that consists of different projects. An i18n string can either
 * contain a project URI or be used directly in a project.
 */
@API
// TODO: Remove extends Translator when NUI is properly integrated
public interface TranslationSystem extends Translator {

    /**
     * @param name
     * @return the project or <code>null</code>.
     */
    TranslationProject getProject(ResourceUrn name);

    /**
     * If no perfect match is found for the default locale, fallback strategies will attempt to find the closest match.
     * @param id must match the pattern <code>${module:project#id}</code>
     * @return the translated string
     */
    String translate(String id);

    /**
     * If no perfect match is found for the given locale, fallback strategies will attempt to find the closest match.
     * @param id must match the pattern <code>${module:project#id}</code>
     * @param locale the target locale
     * @return the translated string
     */
    String translate(String id, Locale locale);

    /**
     * Subscribe to change events. Will be fired when the content of a project is changed.
     * @param changeListener the listener to add
     */
    void subscribe(Consumer<TranslationProject> changeListener);

    /**
     * Unsubscribe from change events.
     * @param reloadListener the listener to remove. Non-existing entries will be ignored.
     */
    void unsubscribe(Consumer<TranslationProject> reloadListener);

    /**
     * Check if the collection of translation assets has changed.
     */
    void refresh();
}
