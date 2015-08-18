/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.i18n.assets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.terasology.assets.AssetData;
import org.terasology.engine.Uri;

import com.google.common.base.Preconditions;


/**
 * Defines translation data in the form of a map {ID -> value}.
 */
public class TranslationData implements AssetData {

    private final Map<String, String> map = new HashMap<>();
    private final Locale locale;
    private final Uri uri;

    /**
     * @param uri the id of the data set, never <code>null</code>.
     * @param locale the locale of the data set, never <code>null</code>.
     */
    public TranslationData(Uri uri, Locale locale) {
        Preconditions.checkArgument(uri != null);
        Preconditions.checkArgument(locale != null);

        this.uri = uri;
        this.locale = locale;
    }

    /**
     * Existing entries will be overwritten.
     * @param entries the entries to add
     */
    public void addAll(Map<String, String> entries) {
        map.putAll(entries);
    }

    /**
     * @return an unmodifiable view on the translation data, never <code>null</code>.
     */
    public Map<String, String> getTranslations() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * @return the locale of this translation data set
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the project uri this data set belongs to.
     */
    public Uri getProjectUri() {
        return uri;
    }
}
