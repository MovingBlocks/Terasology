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
package org.terasology.logic.console.internal;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.registry.CoreRegistry;

import java.util.Set;

/**
 * A utility for registering command classes with the {@link CoreCommand} annotation.
 *
 * @author Limeth
 */
public final class CoreCommands {
	private static final Logger LOGGER         = LoggerFactory.getLogger(CoreCommands.class);
	private static final Reflections REFLECTIONS = new Reflections();

	private CoreCommands() {}

	/**
	 * Registers all commands with the {@link org.terasology.logic.console.internal.CoreCommand} annotation.
	 */
	public static void initialiseCommands() {
		Set<Class<?>> commandClasses = REFLECTIONS.getTypesAnnotatedWith(CoreCommand.class);
		ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);

		for (Class<?> commandClass : commandClasses) {
			if (Command.class.isAssignableFrom(commandClass)) {
				try {
					Command commandObject = (Command) commandClass.newInstance();

					componentSystemManager.register(commandObject);
				} catch (Throwable t) {
					LOGGER.warn("Cannot register core command " + commandClass);
					t.printStackTrace();
				}
			}
		}
	}
}
