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

package org.terasology.i18n.assets;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.Uri;

import com.google.common.base.Preconditions;

/**
 * Defines a translation asset.
 */
public class Translation extends Asset<TranslationData> {

    private final List<Consumer<Translation>> reloadListeners = new CopyOnWriteArrayList<>();

    private TranslationData data;

    /**
     * @param urn       The urn identifying the asset. Never <code>null</code>.
     * @param assetType The asset type this asset belongs to. Never <code>null</code>.
     * @param data      The actual translation data. Never <code>null</code>.
     */
    public Translation(ResourceUrn urn, AssetType<?, TranslationData> assetType, TranslationData data) {
        super(urn, assetType);
        Preconditions.checkArgument(data != null);
        reload(data);
    }

    /**
     * @return the uri of the project this instance is part of
     */
    public Uri getProjectUri() {
        return data.getProjectUri();
    }
    /**
     * @return the locale of the translation data
     */
    public Locale getLocale() {
        return data.getLocale();
    }

    /**
     * Subscribe to reload events.
     * @param reloadListener the listener to add
     */
    public void subscribe(Consumer<Translation> reloadListener) {
        reloadListeners.add(reloadListener);
    }

    /**
     * Unsubscribe from reload events.
     * @param reloadListener the listener to remove. Non-existing entries will be ignored.
     */
    public void unsubscribe(Consumer<Translation> reloadListener) {
        reloadListeners.remove(reloadListener);
    }

    /**
     * Retrieves the matching entry for the given key.
     * @param id the id of the entry
     * @return the translated string
     */
    public String lookup(String id) {
        return data.getTranslations().get(id);
    }

    @Override
    protected void doDispose() {
        this.data = null;
    }

    @Override
    public String toString() {
        return getUrn().toString();
    }

    @Override
    protected void doReload(TranslationData newData) {
        Preconditions.checkArgument(newData != null);
        this.data = newData;
        for (Consumer<Translation> listener : reloadListeners) {
            listener.accept(this);
        }
    }

}
