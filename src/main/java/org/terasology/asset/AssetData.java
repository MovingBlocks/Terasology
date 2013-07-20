/*
 * Copyright 2013 Moving Blocks
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

/**
 * AssetData is the implementation agnostic data for an asset - typically it isn't dependant on either the source format
 * or the implementation consuming the resource. For instance, for a texture the asset data would not depend on the
 * format of the image the texture is sourced from, nor whether textures are handled by LWJGL or some other renderer.
 * <p/>
 * This separation allows support for multiple implementations on either end, as well as the direct procedural creation
 * of assets. It should be noted that there may be multiple implementations of Asset Data in some cases as well, although
 * these cases should be rare.
 * <p/>
 * AssetData is information used to create and reload assets
 */
public interface AssetData {
}
