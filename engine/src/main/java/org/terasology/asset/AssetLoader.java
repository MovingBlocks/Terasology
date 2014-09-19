/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.module.Module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * An asset loader provides the functionality to load some type of asset from a stream, or from a set of urls where
 * an asset is composed of multiple files.
 *
 * @author Immortius
 */
public interface AssetLoader<T extends AssetData> {

    /**
     * Loads an asset. The module containing the asset is provided, along with an stream, and the urls relating to the
     * asset. In simple cases there is just one url, but in some cases an asset is composed of multiple files so the
     * urls for each file is provided. In these cases the stream is for the first url.
     * <p/>
     * The provided stream is cleaned up by the caller, and doesn't have to be closed by this method.
     *
     * @param module The module providing the asset
     * @param stream A stream containing the assets data.
     * @param urls   The urls related to the asset. The first url is the url providing the stream
     * @return The loaded asset, or null
     * @throws IOException If there is any error loading the asset
     */
    T load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException;
}
