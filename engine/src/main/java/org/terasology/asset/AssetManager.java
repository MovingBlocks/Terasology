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

package org.terasology.asset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.terasology.module.ModuleEnvironment;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;

//TODO: Split in two interface with one for loading and adding assets, the other with disposal and other more management methods
@API
public interface AssetManager {
    ModuleEnvironment getEnvironment();

    void setEnvironment(ModuleEnvironment environment);

    <T extends Asset<U>, U extends AssetData> void addResolver(AssetType assetType, AssetResolver<T, U> resolver);

    void setAssetFactory(AssetType type, AssetFactory<?, ?> factory);

    List<AssetUri> resolveAll(AssetType type, String name);

    List<AssetUri> resolveAll(AssetType type, Name name);

    AssetUri resolve(AssetType type, String name);

    <T extends AssetData> T resolveAndLoadData(AssetType type, String name, Class<T> dataClass);

    <T extends AssetData> T resolveAndTryLoadData(AssetType type, String name, Class<T> dataClass);

    AssetData resolveAndLoadData(AssetType type, String name);

    AssetData resolveAndTryLoadData(AssetType type, String name);

    <T extends Asset<?>> T resolveAndLoad(AssetType type, String name, Class<T> assetClass);

    <T extends Asset<?>> T resolveAndTryLoad(AssetType type, String name, Class<T> assetClass);

    Asset<?> resolveAndLoad(AssetType type, String name);

    Asset<?> resolveAndTryLoad(AssetType type, String name);

    <T> T tryLoadAssetData(AssetUri uri, Class<T> type);

    <T> T loadAssetData(AssetUri uri, Class<T> type);

    void register(AssetType type, String extension, AssetLoader<?> loader);

    <T extends Asset<?>> T tryLoadAsset(AssetUri uri, Class<T> type);

    Asset<?> loadAsset(AssetUri uri);

    <D extends AssetData> void reload(Asset<D> asset);

    <T extends Asset<?>> T loadAsset(AssetUri uri, Class<T> assetClass);

    void refresh();

    void dispose(Asset<?> asset);

    <U extends AssetData> Asset<U> generateAsset(AssetUri uri, U data);

    <U extends AssetData> Asset<U> generateTemporaryAsset(AssetType type, U data);

    <D extends AssetData> void removeAssetSource(AssetSource source);

    void applyOverrides();

    Iterable<AssetUri> listAssets();

    Iterable<AssetUri> listAssets(AssetType type);

    <T> Iterable<T> listLoadedAssets(AssetType type, Class<T> assetClass);

    Iterable<Name> listModuleNames();

    List<URL> getAssetURLs(AssetUri uri);

    InputStream getAssetStream(AssetUri uri) throws IOException;

}
