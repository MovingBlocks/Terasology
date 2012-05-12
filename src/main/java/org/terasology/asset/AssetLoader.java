/*
 * Copyright 2012
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

/**
 * @author Immortius
 */
public interface AssetLoader<T extends Asset> {
    /**
     * Loads an asset
     * @param stream A stream containing the assets data.
     * @param uri The uri of the asset
     * @param urls The urls related to the asset. The first url is the url providing the stream
     * @return The loaded asset, or null
     * @throws IOException If there is any error loading the asset
     */
    public T load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException;
}
