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

import java.util.Locale;

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.Uri;

import com.google.common.base.Preconditions;

/**
 * The default implementation of a translation asset.
 */
public final class TranslationImpl extends Translation {

    private TranslationData data;

    /**
     * @param urn       The urn identifying the asset. Never <code>null</code>.
     * @param assetType The asset type this asset belongs to. Never <code>null</code>.
     * @param data      The actual translation data. Never <code>null</code>.
     */
    public TranslationImpl(ResourceUrn urn, AssetType<?, TranslationData> assetType, TranslationData data) {
        super(urn, assetType);
        Preconditions.checkArgument(data != null);
        reload(data);
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
    }

    @Override
    public Locale getLocale() {
        return data.getLocale();
    }

    @Override
    public Uri getProjectUri() {
        return data.getProjectUri();
    }

    @Override
    public String lookup(String id) {
        return data.getTranslations().get(id);
    }
}
