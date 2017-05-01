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
import java.util.List;

import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import com.google.common.collect.Lists;

public class AssetSourceCollection implements AssetSource {

    private String sourceId;
    private AssetSource assetSource1;
    private AssetSource assetSource2;

    public AssetSourceCollection(String sourceId, AssetSource assetSource1, AssetSource assetSource2) {
        this.sourceId = sourceId;
        this.assetSource1 = assetSource1;
        this.assetSource2 = assetSource2;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public List<URL> get(AssetUri uri) {
        List<URL> list1 = assetSource1.get(uri);
        List<URL> list2 = assetSource2.get(uri);
        List<URL> combinedList = Lists.newArrayList(list1);
        combinedList.addAll(list2);
        return combinedList;
    }

    @Override
    public Iterable<AssetUri> list() {
        List<AssetUri> combinedList = createCombinedList(assetSource1.list(), assetSource2.list());
        return combinedList;
    }

    @Override
    public Iterable<AssetUri> list(AssetType type) {
        List<AssetUri> combinedList = createCombinedList(assetSource1.list(type), assetSource2.list(type));
        return combinedList;
    }

    @Override
    public List<URL> getOverride(AssetUri uri) {
        List<URL> list1 = assetSource1.getOverride(uri);
        List<URL> list2 = assetSource2.getOverride(uri);
        List<URL> combinedList = Lists.newArrayList(list1);
        combinedList.addAll(list2);
        return combinedList;
    }

    @Override
    public Iterable<AssetUri> listOverrides() {
        List<AssetUri> combinedList = createCombinedList(assetSource1.listOverrides(), assetSource2.listOverrides());
        return combinedList;
    }

    private List<AssetUri> createCombinedList(Iterable<AssetUri> iterable1, Iterable<AssetUri> iterable2) {
        List<AssetUri> combinedList = Lists.newArrayList();
        if (null != iterable1) {
            for (AssetUri assetUri : iterable1) {
                combinedList.add(assetUri);
            }
        }
        if (null != iterable2) {
            for (AssetUri assetUri : iterable2) {
                combinedList.add(assetUri);
            }
        }
        return combinedList;
    }

}
