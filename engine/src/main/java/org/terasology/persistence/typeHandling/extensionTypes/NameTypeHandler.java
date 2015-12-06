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

package org.terasology.persistence.typeHandling.extensionTypes;

import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

/**
 */
public class NameTypeHandler extends StringRepresentationTypeHandler<Name> {

    @Override
    public String getAsString(Name item) {
        if (item == null) {
            return "";
        }
        return item.toString();
    }

    @Override
    public Name getFromString(String representation) {
        return new Name(representation);
    }
}
