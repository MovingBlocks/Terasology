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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.i18n.assets.Translation;
import org.terasology.naming.Name;

/**
 * TODO Type description
 */
public class TranslationSystemImpl implements TranslationSystem {

    private static final Logger logger = LoggerFactory.getLogger(TranslationSystemImpl.class);

    private final AssetManager assetManager;
    private final Map<Name, TranslationProject> projects = new HashMap<>();

    private Locale locale;

    public TranslationSystemImpl(Context context) {

        locale = Locale.getDefault(Locale.Category.DISPLAY);
        assetManager = context.get(AssetManager.class);

        Set<ResourceUrn> urns = assetManager.getAvailableAssets(Translation.class);
        for (ResourceUrn urn : urns) {
            Optional<Translation> asset = assetManager.getAsset(urn, Translation.class);
            if (asset.isPresent()) {
                Translation trans = asset.get();
                Name name = trans.getName();
                TranslationProject proj = projects.computeIfAbsent(name, e -> new StandardTranslationProject(locale));
                proj.add(trans);
                logger.info("Discovered " + trans);
            }
        }
    }

    @Override
    public TranslationProject getProject(Name name) {
        return projects.get(name);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        for (TranslationProject project : projects.values()) {
            project.setLocale(locale);
        }
    }
}
