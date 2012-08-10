/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.world.block;

import com.google.common.base.Objects;

import java.util.Locale;

/**
 * Identifier for both blocks and block families.
 * The block uri has a pattern of:
 * [package]:[blockFamily].[blockIdentifier].
 * When looking up a block family, the first two parts are used, for a block all three.
 * e.g.
 * engine:brickstair.left for left-aligned stairs
 *
 * @author Immortius
 */
public class BlockUri {
    private static final String PACKAGE_SEPARATOR = ":";
    private static final String IDENTIFIER_SEPARATOR = ".";
    private static final String IDENTIFIER_SEPARATOR_REGEX = "\\.";

    private String packageName = "";
    private String familyName = "";
    private String blockIdentifier = "";

    public BlockUri(String packageName, String familyName) {
        this.packageName = packageName.toLowerCase(Locale.ENGLISH);
        this.familyName = familyName.toLowerCase(Locale.ENGLISH);
    }

    public BlockUri(String packageName, String familyName, String identifier) {
        this(packageName, familyName);
        this.blockIdentifier = identifier.toLowerCase(Locale.ENGLISH);
    }

    public BlockUri(BlockUri familyUri, String identifier) {
        packageName = familyUri.packageName;
        familyName = familyUri.familyName;
        blockIdentifier = identifier.toLowerCase(Locale.ENGLISH);
    }

    public BlockUri(String uri) {
        String[] split = uri.toLowerCase(Locale.ENGLISH).split(PACKAGE_SEPARATOR, 2);
        if (split.length > 1) {
            packageName = split[0];
            split = split[1].split(IDENTIFIER_SEPARATOR_REGEX, 2);
            if (split.length > 1) {
                familyName = split[0];
                blockIdentifier = split[1];
            } else if (split.length == 1) {
                familyName = split[0];
            }
        }
    }

    public boolean isValid() {
        return !packageName.isEmpty() && !familyName.isEmpty();
    }

    public String getPackage() {
        return packageName;
    }

    public String getFamily() {
        return familyName;
    }

    public String getIdentifier() {
        return blockIdentifier;
    }

    public BlockUri getFamilyUri() {
        if (blockIdentifier.isEmpty()) {
            return this;
        }
        return new BlockUri(packageName, familyName);
    }

    @Override
    public String toString() {
        if (blockIdentifier.isEmpty()) {
            return packageName + PACKAGE_SEPARATOR + familyName;
        }
        return packageName + PACKAGE_SEPARATOR + familyName + IDENTIFIER_SEPARATOR + blockIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockUri) {
            BlockUri other = (BlockUri) obj;
            return Objects.equal(other.packageName, packageName) && Objects.equal(other.familyName, familyName) && Objects.equal(other.blockIdentifier, blockIdentifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packageName, familyName, blockIdentifier);
    }

}
