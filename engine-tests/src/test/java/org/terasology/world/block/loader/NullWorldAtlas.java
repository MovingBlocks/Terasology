/*
 * Copyright (C) 2012-2014 Martin Steiger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terasology.world.block.loader;

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetUri;

/**
 * Dummy implementation of WorldAtlas
 * @author Martin Steiger
 */
public class NullWorldAtlas extends WorldAtlas {

    public NullWorldAtlas() {
        super(0);
    }

    @Override
    public int getTileSize() {
        return 0;
    }

    @Override
    public int getAtlasSize() {
        return 0;
    }

    @Override
    public float getRelativeTileSize() {
        return 0;
    }

    @Override
    public int getNumMipmaps() {
        return 0;
    }

    @Override
    public Vector2f getTexCoords(AssetUri uri, boolean warnOnError) {
        return new Vector2f();
    }

    
}
