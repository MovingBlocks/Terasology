/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.asset.sources;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import com.google.common.collect.Lists;

public class AssetSourceCollection implements AssetSource {

    private String sourceId;
    private Iterable<AssetSource> assetSources;

    public AssetSourceCollection(String sourceId, Iterable<AssetSource> assetSources) {
        this.sourceId = sourceId;
        this.assetSources = assetSources;
    }

    public AssetSourceCollection(String sourceId, AssetSource ... assetSources) {
        this(sourceId, Arrays.asList(assetSources));
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public List<URL> get(AssetUri uri) {
        List<URL> combinedList = Lists.newArrayList();
        for (AssetSource assetSource : assetSources) {
            combinedList.addAll(assetSource.get(uri));
        }
        return combinedList;
    }

    @Override
    public Iterable<AssetUri> list() {
        List<AssetUri> combinedList = Lists.newArrayList();
        for (AssetSource assetSource : assetSources) {
            for (AssetUri assetUri : assetSource.list()) {
                combinedList.add(assetUri);
            }
        }
        return combinedList;
    }

    @Override
    public Iterable<AssetUri> list(AssetType type) {
        List<AssetUri> combinedList = Lists.newArrayList();
        for (AssetSource assetSource : assetSources) {
            for (AssetUri assetUri : assetSource.list(type)) {
                combinedList.add(assetUri);
            }
        }
        return combinedList;
    }

    @Override
    public List<URL> getOverride(AssetUri uri) {
        List<URL> combinedList = Lists.newArrayList();
        for (AssetSource assetSource : assetSources) {
            combinedList.addAll(assetSource.getOverride(uri));
        }
        return combinedList;
    }

    @Override
    public Iterable<AssetUri> listOverrides() {
        List<AssetUri> combinedList = Lists.newArrayList();
        for (AssetSource assetSource : assetSources) {
            for (AssetUri assetUri : assetSource.listOverrides()) {
                combinedList.add(assetUri);
            }
        }
        return combinedList;
    }

    @Override
    public List<URL> getDelta(AssetUri uri) {
        List<URL> combinedList = Lists.newArrayList();
        for (AssetSource assetSource : assetSources) {
            combinedList.addAll(assetSource.getDelta(uri));
        }
        return combinedList;
    }

}
