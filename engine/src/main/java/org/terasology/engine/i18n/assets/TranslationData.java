// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.i18n.assets;

import com.google.common.base.Preconditions;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Defines translation data in the form of a map {ID → value}.
 */
public class TranslationData implements AssetData {

    private final Map<String, String> map = new HashMap<>();
    private final Locale locale;
    private final ResourceUrn urn;

    /**
     * @param urn the id of the data set, never <code>null</code>.
     * @param locale the locale of the data set, never <code>null</code>.
     */
    public TranslationData(ResourceUrn urn, Locale locale) {
        Preconditions.checkArgument(urn != null);
        Preconditions.checkArgument(locale != null);

        this.urn = urn;
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
    public ResourceUrn getProjectUrn() {
        return urn;
    }
}
