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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.internal.commands.*;
import org.terasology.registry.CoreRegistry;

/**
 * @author Limeth
 */
@RegisterSystem
public class CoreCommands {
	private static final Logger LOGGER         = LoggerFactory.getLogger(CoreCommands.class);
	private static final String TARGET_PACKAGE = CoreCommands.class.getPackage().getName() + ".commands";
/*	private static final ClassLoader[] CLASSLOADERS = new ClassLoader[] {
			ClasspathHelper.contextClassLoader(),
			ClasspathHelper.staticClassLoader()
	};
	private static final Reflections REFLECTIONS = new Reflections(new ConfigurationBuilder()
			                         .setScanners(new SubTypesScanner(false *//* don't exclude Object.class *//*), new ResourcesScanner())
			                         .setUrls(ClasspathHelper.forClassLoader(CLASSLOADERS))
			                         .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(TARGET_PACKAGE))));*/
	@SuppressWarnings("unchecked")
	public static final Class<? extends Command>[] COMMAND_CLASSES = new Class[] {
			DebugTargetCommand.class, DestroyEntitiesUsingPrefabCommand.class, DumpEntitiesCommand.class,
			EnableAutoScreenReloadingCommand.class, ExitCommand.class, FullscreenCommand.class, HelpCommand.class,
			JoinCommand.class, KillCommand.class, LeaveCommand.class, ReloadMaterialCommand.class,
			ReloadShaderCommand.class, ReloadSkinCommand.class, ReloadUICommand.class, SetWorldTimeCommand.class,
			SpawnBlockCommand.class, SpawnPrefabCommand.class, TeleportCommand.class
	};

	public static void initialiseCommands() {
/*
		Set<Class<?>> commandClasses = REFLECTIONS.getSubTypesOf(Object.class);
*/
		Class<? extends Command>[] commandClasses = COMMAND_CLASSES;
		ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);

		for (Class<? extends Command> commandClass : commandClasses) {
			try {
				Command commandObject = commandClass.newInstance();

				componentSystemManager.register(commandObject);
			} catch (Throwable t) {
				LOGGER.warn("Cannot register core command " + commandClass);
				t.printStackTrace();
			}
		}
	}
}
