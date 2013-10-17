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
package org.terasology.classMetadata.copying.strategy;

import com.google.common.collect.Lists;
import org.terasology.classMetadata.copying.CopyStrategy;

import java.util.List;

/**
 * @author Immortius
 */
public class ListCopyStrategy<T> implements CopyStrategy<List<T>> {

    private final CopyStrategy<T> contentStrategy;

    public ListCopyStrategy(CopyStrategy<T> contentStrategy) {
        this.contentStrategy = contentStrategy;
    }

    @Override
    public List<T> copy(List<T> value) {
        if (value != null) {
            List<T> result = Lists.newArrayListWithCapacity(value.size());
            for (T item : value) {
                result.add(contentStrategy.copy(item));
            }
            return result;
        }
        return null;
    }
}
