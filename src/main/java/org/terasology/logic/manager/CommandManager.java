/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.manager;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandParam;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;

/**
 * The command manager handles the loading of commands which can be executed through the in-game chat.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class CommandManager {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final List<CommandInfo> commands = new ArrayList<CommandInfo>();
    private final Table<String, Integer, CommandInfo> commandLookup = HashBasedTable.create();

    private final static String bindName = "command";

    /**
     * Defines a command which can be executed through the in-game chat.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    public class CommandInfo {
        private CommandProvider provider;

        private String name;
        private String[] parameterNames;
        private String shortDescription;
        private String helpText;

        public CommandInfo(Method method, CommandProvider provider) {
            this.provider = provider;
            this.name = method.getName();
            this.parameterNames = new String[method.getParameterTypes().length];
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                parameterNames[i] = method.getParameterTypes()[i].toString();
                for (Annotation paramAnnot : method.getParameterAnnotations()[i]) {
                    if (paramAnnot instanceof CommandParam) {
                        parameterNames[i] = ((CommandParam)paramAnnot).name();
                        break;
                    }
                }
            }
            Command commandAnnotation = method.getAnnotation(Command.class);
            this.shortDescription = commandAnnotation.shortDescription();
            this.helpText = commandAnnotation.helpText();
        }

        public String getName() {
            return name;
        }

        public String[] getParameterNames() {
            return parameterNames;
        }

        public int getParameterCount() {
            return parameterNames.length;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public String getHelpText() {
            return helpText;
        }

        public String getUsageMessage() {
            StringBuilder builder = new StringBuilder(name);
            for (String param : parameterNames) {
                builder.append(" <");
                builder.append(param);
                builder.append(">");
            }

            return builder.toString();
        }

        /**
         * Execute the method which is assigned to the command.
         *
         * @param params
         */
        public void methodExecute(String params) {

            Binding bind = new Binding();
            bind.setVariable(bindName, provider);
            GroovyShell shell = new GroovyShell(bind);

            logger.log(Level.INFO, bindName + "." + name + "(" + params + ")");
            shell.evaluate(bindName + "." + name + "(" + params + ")");
        }
    }

    /**
     * Create an instance of the CommandManager which will load all commands.
     */
    public CommandManager() {
        loadCommands();
    }

    /**
     * Load all default and mod commands from the JSON files.
     */
    private void loadCommands() {
        Set<URL> classpathURLs = Sets.newHashSet();
        List<ClassLoader> classLoaders = Lists.newArrayList();
        classpathURLs.add(ClasspathHelper.forClass(CommandManager.class));
        for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
            classpathURLs.add(mod.getModClasspathUrl());
            classLoaders.add(mod.getClassLoader());
        }
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(
                        new SubTypesScanner(),
                        new MethodAnnotationsScanner())
                .setUrls(classpathURLs).addClassLoaders(classLoaders));
        for (Class<? extends CommandProvider> providerClass : reflections.getSubTypesOf(CommandProvider.class)) {
            try {
                CommandProvider provider = providerClass.newInstance();

                Predicate<? super Method> predicate = Predicates.<Method>and(withModifier(Modifier.PUBLIC), withAnnotation(Command.class));
                Set<Method> commandMethods = Reflections.getAllMethods(providerClass, predicate);
                for (Method method : commandMethods) {
                    CommandInfo command = new CommandInfo(method, provider);
                    commands.add(command);
                    commandLookup.put(command.getName(), command.getParameterCount(), command);
                }
            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Failed to instantiate command provider " + providerClass.getName(), e);
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Failed to instantiate command provider " + providerClass.getName(), e);
            }
        }

        //sort commands by their names
        Comparator<CommandInfo> comp = new Comparator<CommandInfo>() {
            @Override
            public int compare(CommandInfo o1, CommandInfo o2) {
                int nameComp = o1.getName().compareTo(o2.getName());
                if (nameComp == 0) {
                    return o1.getParameterCount() - o2.getParameterCount();
                }
                return nameComp;
            }
        };

        Collections.sort(commands, comp);
    }

    /**
     * Execute a command.
     *
     * @param str The whole string of the command including the command name and the optional parameters. Without "/" at the beginning.
     * @return Returns true if the command was executed successfully.
     */
    public boolean execute(String str) {
        //remove double spaces
        str = str.replaceAll("\\s+", " ");

        //get the command name
        int commandEndIndex = str.indexOf(" ");
        String commandName;
        if (commandEndIndex >= 0) {
            commandName = str.substring(0, commandEndIndex);
        } else {
            commandName = str;
            str = "";
            commandEndIndex = 0;
        }

        //remove command name from string
        str = str.substring(commandEndIndex).trim();

        //get the parameters
        String[] params = str.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        String paramsStr = "";
        int paramsCount = 0;

        for (String s : params) {
            if (s.trim().isEmpty()) {
                continue;
            }

            if (!paramsStr.isEmpty()) {
                paramsStr += ",";
            }
            paramsStr += s;
            paramsCount++;
        }

        //get the command
        CommandInfo cmd = commandLookup.get(commandName, paramsCount);

        //check if the command is loaded
        if (cmd == null) {
            /* TODO: The direct dependency between the command manager and a message manager singleton is not a good idea,
               would suggest returning it as part of the result instead (need less singletons!) */
            if (commandLookup.containsRow(commandName)) {
                MessageManager.getInstance().addMessage("Incorrect number of parameters");
            } else {
                MessageManager.getInstance().addMessage("Unknown command '" + commandName + "'");
            }

            return false;
        }
        String executeString = paramsStr;
        logger.log(Level.INFO, "Execute command with params '" + paramsStr + "'");

        try {
            //execute the command
            cmd.methodExecute(paramsStr);

            return true;
        } catch (Exception e) {
            //TODO better error handling and error message
            MessageManager.getInstance().addMessage(cmd.getUsageMessage());
            MessageManager.getInstance().addMessage("Error executing command '" + commandName + "'.");
            logger.log(Level.WARNING, "Failed to run command", e);

            return false;
        }
    }

    /**
     * Get a group of commands by their name. These will vary by the number of parameters they accept
     *
     * @param name The name of the command.
     * @return An iterator over the commands.
     */
    public Collection<CommandInfo> getCommand(String name) {
        return commandLookup.row(name).values();
    }

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    public List<CommandInfo> getCommandList() {
        return commands;
    }
}
