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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.registry.CoreRegistry;

import java.util.Objects;

/**
 * Base class for all Assets that ensures that they
 * @author Immortius <immortius@gmail.com>
 * @author Florian <florian@fkoeberle.de>
 */
public abstract class AbstractAsset<T extends AssetData> implements Asset<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAsset.class);
    private final AssetUri uri;

    public AbstractAsset(AssetUri uri) {
        this.uri = uri;
    }

    private boolean disposed;

    /**
     * @return This asset's identifying URI.
     */
    @Override
    public final AssetUri getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof  AbstractAsset) {
            return Objects.equals(uri, ((AbstractAsset<?>) obj).uri);
        }
        return false;
    }

    /**
     * Reload the specified data. The asset must not be disposed yet!
     */
    public final void reload(T data) {
        if (!disposed) {
            onReload(data);
            if (disposed) {
                logger.error("Asset {} got illegally disposed in its onReload method", getURI());
            }
        } else {
            logger.error("Disposed asset with uri {} can't be reloaded", getURI());
        }
    }

    /**
     * Reload the specified data. The implementation of this method must not call dispose.
     * @param data
     */
    protected abstract void onReload(T data);


    /**
     * Disposes the asset. Must not be called from onReload.
     */
    @Override
    public final void dispose() {
        if (!disposed) {
            onDispose();
            disposed = true;
            CoreRegistry.get(AssetManager.class).dispose(this);
        }
    }

    protected abstract void onDispose();

    @Override
    public int hashCode() {
        return uri.hashCode();
    }


    @Override
    public final boolean isDisposed() {
        return disposed;
    }
}
