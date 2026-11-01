// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n.assets;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.naming.Name;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Defines a translation asset.
 */
public class Translation extends Asset<TranslationData> {

    private Map<String, String> dictionary = new HashMap<>();
    private Locale locale;
    private ResourceUrn projectUrn;

    private final DisposalAction disposalAction;

    /**
     * @param urn       The urn identifying the asset. Never <code>null</code>.
     * @param assetType The asset type this asset belongs to. Never <code>null</code>.
     * @param data      The actual translation data. Never <code>null</code>.
     */
    public Translation(ResourceUrn urn, AssetType<?, TranslationData> assetType, TranslationData data, Translation.DisposalAction disposalAction) {
        super(urn, assetType);
        setDisposableResource(disposalAction);
        this.disposalAction = disposalAction;
        this.disposalAction.setAsset(this);
        reload(data);
    }

    /**
     * Factory method for Translation
     *
     * @param urn The urn identifying the asset. Never <code>null</code>.
     * @param assetType The asset type this asset belongs to. Never <code>null</code>.
     * @param data The actual translation data. Never <code>null</code>.
     */
    public static Translation create(ResourceUrn urn, AssetType<?, TranslationData> assetType, TranslationData data) {
        return new Translation(urn, assetType, data, new DisposalAction());
    }

    /**
     * @return the uri of the project this instance is part of
     */
    public ResourceUrn getProjectUrn() {
        return projectUrn;
    }
    /**
     * @return the locale of the translation data
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Subscribe to reload/dispose events.
     * @param changeListener the listener to add
     */
    public void subscribe(Consumer<Translation> changeListener) {
        disposalAction.changeListeners.add(changeListener);
    }

    /**
     * Unsubscribe from reload/dispose events.
     * @param changeListener the listener to remove. Non-existing entries will be ignored.
     */
    public void unsubscribe(Consumer<Translation> changeListener) {
        disposalAction.changeListeners.remove(changeListener);
    }

    /**
     * Retrieves the matching entry for the given key.
     * @param id the id of the entry
     * @return the translated string
     */
    public String lookup(Name id) {
        return dictionary.get(id.toString());
    }

    @Override
    public String toString() {
        return getUrn().toString();
    }

    @Override
    protected void doReload(TranslationData data) {
        Preconditions.checkArgument(data != null);

        boolean isEqual = Objects.equal(data.getProjectUrn(), projectUrn)
                && Objects.equal(data.getLocale(), locale)
                && Objects.equal(data.getTranslations(), dictionary);

        if (!isEqual) {
            this.dictionary.clear();
            this.dictionary.putAll(data.getTranslations());
            this.projectUrn = data.getProjectUrn();
            this.locale = data.getLocale();

            for (Consumer<Translation> listener : disposalAction.changeListeners) {
                listener.accept(this);
            }
        }
    }

    private static class DisposalAction implements DisposableResource {

        private final List<Consumer<Translation>> changeListeners = new CopyOnWriteArrayList<>();
        private WeakReference<Translation> asset;

         DisposalAction() {
        }

        public void setAsset(Translation asset) {
            this.asset = new WeakReference<>(asset);
        }


        @Override
        public void close() {
            Translation translation = asset.get();
            if (translation != null) {
                for (Consumer<Translation> listener : changeListeners) {
                    listener.accept(translation);
                }
            }
        }
    }

}
