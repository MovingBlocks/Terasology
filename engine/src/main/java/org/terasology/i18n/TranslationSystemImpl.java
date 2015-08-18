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
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Uri;
import org.terasology.i18n.assets.Translation;

import com.google.common.base.Preconditions;

/**
 * TODO Type description
 */
public class TranslationSystemImpl implements TranslationSystem {

    private static final Logger logger = LoggerFactory.getLogger(TranslationSystemImpl.class);

    private final AssetManager assetManager;
    private final Map<Uri, TranslationProject> projects = new HashMap<>();

    private Locale locale;

    /**
     * @param context the context to use
     */
    public TranslationSystemImpl(Context context) {
        this(context, Locale.getDefault(Locale.Category.DISPLAY));
    }

    /**
     * @param context the context to use
     * @param locale the default locale to use
     */
    public TranslationSystemImpl(Context context, Locale locale) {

        assetManager = context.get(AssetManager.class);
        this.locale = locale;

        Set<ResourceUrn> urns = assetManager.getAvailableAssets(Translation.class);
        for (ResourceUrn urn : urns) {
            Optional<Translation> asset = assetManager.getAsset(urn, Translation.class);
            if (asset.isPresent()) {
                Translation trans = asset.get();
                Uri uri = trans.getProjectUri();
                if (uri.isValid()) {
                    TranslationProject proj = projects.computeIfAbsent(uri, e -> new StandardTranslationProject());
                    proj.add(trans);
                    logger.info("Discovered " + trans);
                } else {
                    logger.warn("Ignoring invalid project uri: {}", uri);
                }
            }
        }
    }

    @Override
    public TranslationProject getProject(Uri name) {
        return projects.get(name);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        Preconditions.checkArgument(locale != null);
        this.locale = locale;
    }

    @Override
    public String translate(String id) {
        return translate(id, locale);
    }

    @Override
    public String translate(String id, Locale otherLocale) {
        int splitPoint = id.indexOf('#');
        if (splitPoint > 0) {
            String projName = id.substring(0, splitPoint);
            String fragment = id.substring(splitPoint + 1);
            SimpleUri uri = new SimpleUri(projName);
            TranslationProject project = getProject(uri);
            if (project != null) {
                return project.translate(fragment, otherLocale);
            } else {
                logger.warn("Invalid project for '{}'", id);
            }
        }
        return null;
    }
}
