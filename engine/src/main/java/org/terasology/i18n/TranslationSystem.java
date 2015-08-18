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

import java.util.Locale;

import org.terasology.engine.Uri;

/**
 * TODO Type description
 */
public interface TranslationSystem {

    /**
     * @param name
     * @return the project or <code>null</code>.
     */
    TranslationProject getProject(Uri name);

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
}
