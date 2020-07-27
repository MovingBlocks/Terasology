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
package org.terasology.reflection.copy.strategy;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.copy.CopyStrategy;

/**
 */
public class MappedContainerCopyStrategy<T> implements CopyStrategy<T> {

    private ClassMetadata<T, ?> classMetadata;

    public MappedContainerCopyStrategy(ClassMetadata<T, ?> classMetadata) {
        this.classMetadata = classMetadata;
    }

    @Override
    public T copy(T value) {
        if (value != null) {
            return classMetadata.copy(value);
        }
        return null;
    }
}
