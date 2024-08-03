// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.i18n.assets.Translation;
import org.terasology.engine.persistence.TemplateEngine;
import org.terasology.engine.persistence.TemplateEngineImpl;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A translation system that uses {@link Translation} data assets to perform the lookup.
 */
public class TranslationSystemImpl implements TranslationSystem {

    private static final Logger logger = LoggerFactory.getLogger(TranslationSystemImpl.class);

    private final List<Consumer<TranslationProject>> changeListeners = new CopyOnWriteArrayList<>();
    private final Map<ResourceUrn, TranslationProject> projects = new HashMap<>();

    private final SystemConfig systemConfig;

    private AssetManager assetManager;

    /**
     * @param context the context to use
     */
    public TranslationSystemImpl(Context context) {

        systemConfig = context.get(SystemConfig.class);
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
                ResourceUrn projectUrn = trans.getProjectUrn();
                if (!projectUrn.getModuleName().isEmpty() && !projectUrn.getResourceName().isEmpty()) {
                    TranslationProject proj = projects.computeIfAbsent(projectUrn, e -> new StandardTranslationProject());
                    proj.add(trans);
                    trans.subscribe(this::onAssetChanged);
                    logger.info("Discovered {}", trans);
                } else {
                    logger.warn("Ignoring invalid project projectUrn: {}", projectUrn);
                }
            }
        }
    }

    @Override
    public TranslationProject getProject(ResourceUrn name) {
        return projects.get(name);
    }

    @Override
    public String translate(String id) {
        return translate(id, systemConfig.locale.get());
    }

    @Override
    public String translate(String text, Locale otherLocale) {
        TemplateEngine templateEngine = new TemplateEngineImpl(id -> {
            ResourceUrn uri = new ResourceUrn(id);
            ResourceUrn projectUri = new ResourceUrn(uri.getModuleName(), uri.getResourceName());
            TranslationProject project = getProject(projectUri);
            if (project != null) {
                Optional<String> opt = project.translate(uri.getFragmentName(), otherLocale);
                if (opt.isPresent()) {
                    return opt.get();
                } else {
                    logger.warn("No translation for '{}'", id);
                    return "?" + uri.getFragmentName() + "?";
                }
            } else {
                logger.warn("Invalid project id '{}'", id);
                return "?" + uri.getFragmentName() + "?";
            }
        });

        return templateEngine.transform(text);
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
        ResourceUrn uri = trans.getProjectUrn();
        TranslationProject project = projects.get(uri);
        if (trans.isDisposed()) {
            project.remove(trans);
        }
        for (Consumer<TranslationProject> listener : changeListeners) {
            listener.accept(project);
        }
    }
}
