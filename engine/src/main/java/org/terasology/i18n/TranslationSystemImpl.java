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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.Config;
import org.terasology.config.SystemConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Uri;
import org.terasology.i18n.assets.Translation;

/**
 * A translation system that uses {@link Translation} data assets to
 * perform the lookup.
 */
public class TranslationSystemImpl implements TranslationSystem {

    private static final Logger logger = LoggerFactory.getLogger(TranslationSystemImpl.class);

    /**
     * The unescaped pattern is <code>${[^}]+})}</code>. Searches for <code>${text}<code> expressions.
     */
    private static final Pattern ID_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private final List<Consumer<TranslationProject>> changeListeners = new CopyOnWriteArrayList<>();
    private final Map<Uri, TranslationProject> projects = new HashMap<>();

    private final SystemConfig config;

    private AssetManager assetManager;

    /**
     * @param context the context to use
     */
    public TranslationSystemImpl(Context context) {

        config = context.get(Config.class).getSystem();
        assetManager = context.get(AssetManager.class);

        refresh();
    }

    @Override
    public void refresh() {
        Set<ResourceUrn> urns = assetManager.getAvailableAssets(Translation.class);
        for (ResourceUrn urn : urns) {
            Optional<Translation> asset = assetManager.getAsset(urn, Translation.class);
            if (asset.isPresent()) {
                Translation trans = asset.get();
                Uri uri = trans.getProjectUri();
                if (uri.isValid()) {
                    TranslationProject proj = projects.computeIfAbsent(uri, e -> new StandardTranslationProject());
                    proj.add(trans);
                    trans.subscribe(this::onAssetChanged);
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
    public String translate(String id) {
        return translate(id, config.getLocale());
    }

    @Override
    public String translate(String text, Locale otherLocale) {
        int cursor = 0;

        Matcher m = ID_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            sb.append(text, cursor, m.start());

            String id = m.group(1);
            ResourceUrn uri = new ResourceUrn(id);
            SimpleUri projectUri = new SimpleUri(uri.getModuleName(), uri.getResourceName());
            TranslationProject project = getProject(projectUri);
            if (project != null) {
                Optional<String> opt = project.translate(uri.getFragmentName(), otherLocale);
                if (opt.isPresent()) {
                    sb.append(opt.get());
                } else {
                    logger.warn("No translation for '{}'", id);
                    sb.append("?" + uri.getFragmentName() + "?");
                }
            } else {
                logger.warn("Invalid project id '{}'", id);
                sb.append("?" + uri.getFragmentName() + "?");
            }
            cursor = m.end();
        }

        sb.append(text, cursor, text.length());
        return sb.toString();
    }

    @Override
    public void subscribe(Consumer<TranslationProject> reloadListener) {
        changeListeners.add(reloadListener);
    }

    @Override
    public void unsubscribe(Consumer<TranslationProject> reloadListener) {
        changeListeners.remove(reloadListener);
    }

    private void onAssetChanged(Translation trans) {
        Uri uri = trans.getProjectUri();
        TranslationProject project = projects.get(uri);
        if (trans.isDisposed()) {
            project.remove(trans);
        }
        for (Consumer<TranslationProject> listener : changeListeners) {
            listener.accept(project);
        }
    }
}
