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
package org.terasology.logic.console.commands;

import com.google.common.base.Optional;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * Classes extending this interface occur in command method parameter definition
 *
 * @author Limeth
 */
interface CommandParameterType {
    Optional<? extends Class<?>> getProvidedType();

    /**
     * Used if the parameter of {@link ReferencedCommand}
     * has a {@link org.terasology.logic.console.commands.referenced.Sender} annotation
     */
    public final class SenderParameterType implements CommandParameterType {
        SenderParameterType() {
        }

        @Override
        public Optional<? extends Class<?>> getProvidedType() {
            return Optional.of(EntityRef.class);
        }
    }

    /**
     * Used if the parameter of {@link ReferencedCommand} doesn't have
     * a {@link org.terasology.logic.console.commands.referenced.CommandDefinition}
     * nor a {@link org.terasology.logic.console.commands.referenced.Sender} annotation.
     */
    public final class InvalidParameterType implements CommandParameterType {
        InvalidParameterType() {
        }

        @Override
        public Optional<? extends Class<?>> getProvidedType() {
            return Optional.absent();
        }
    }
}
