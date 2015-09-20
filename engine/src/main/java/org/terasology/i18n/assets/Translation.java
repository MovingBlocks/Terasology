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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.Uri;
import org.terasology.naming.Name;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Defines a translation asset.
 */
public class Translation extends Asset<TranslationData> {

    private Map<String, String> dictionary = new HashMap<>();
    private Locale locale;
    private Uri projectUri;

    private final DisposalAction disposalAction;

    /**
     * @param urn       The urn identifying the asset. Never <code>null</code>.
     * @param assetType The asset type this asset belongs to. Never <code>null</code>.
     * @param data      The actual translation data. Never <code>null</code>.
     */
    public Translation(ResourceUrn urn, AssetType<?, TranslationData> assetType, TranslationData data) {
        super(urn, assetType);
        this.disposalAction = new DisposalAction(this);
        getDisposalHook().setDisposeAction(disposalAction);
        reload(data);
    }

    /**
     * @return the uri of the project this instance is part of
     */
    public Uri getProjectUri() {
        return projectUri;
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

        boolean isEqual = Objects.equal(data.getProjectUri(), projectUri)
                && Objects.equal(data.getLocale(), locale)
                && Objects.equal(data.getTranslations(), dictionary);

        if (!isEqual) {
            this.dictionary.clear();
            this.dictionary.putAll(data.getTranslations());
            this.projectUri = data.getProjectUri();
            this.locale = data.getLocale();

            for (Consumer<Translation> listener : disposalAction.changeListeners) {
                listener.accept(this);
            }
        }
    }

    private static class DisposalAction implements Runnable {

        private final List<Consumer<Translation>> changeListeners = new CopyOnWriteArrayList<>();
        private final WeakReference<Translation> asset;

        public DisposalAction(Translation asset) {
            this.asset = new WeakReference<>(asset);
        }

        @Override
        public void run() {
            Translation translation = asset.get();
            if (translation != null) {
                for (Consumer<Translation> listener : changeListeners) {
                    listener.accept(translation);
                }
            }
        }
    }

}
