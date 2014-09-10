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

import org.terasology.naming.Name;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
public interface AssetSource {

    /**
     * @return The identifier for this asset source
     */
    Name getSourceId();

    /**
     * The URL(s) related to a URI. There may be multiple
     *
     * @param uri
     * @return The url equivalent of this uri
     */
    List<URL> get(AssetUri uri);

    Iterable<AssetUri> list();

    Iterable<AssetUri> list(AssetType type);

    List<URL> getOverride(AssetUri uri);

    Iterable<AssetUri> listOverrides();

    Collection<URL> getDelta(AssetUri uri);

}
