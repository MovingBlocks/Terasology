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
import java.util.Map.Entry;

import org.terasology.i18n.assets.Translation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * TODO Type description
 */
public class StandardTranslationProject implements TranslationProject {

    private final Table<String, Locale, String> table = HashBasedTable.create();
    private Locale locale;

    public StandardTranslationProject(Locale locale) {
        setLocale(locale);
    }

    @Override
    public void add(Translation trans) {
        for (Entry<String, String> entry : trans.getTranslations().entrySet()) {
            table.put(entry.getKey(), trans.getLocale(), entry.getValue());
        }
    }

    @Override
    public String translate(String screenId, String widgetId, String fragment) {
        String key = screenId + "#" + widgetId + "#" + fragment;
        I18nMap mappedId = new I18nMap(table.row(key));
        String value = mappedId.valueFor(locale);
        return value;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
