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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terasology.logic.commands.CommandController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The command manager handles the loading of commands which can be executed through the in-game chat.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 */
public class CommandManager {
    
    private static CommandManager instance;
    private final Map<String, CommandController> controllers = new HashMap<String, CommandController>();
    private final List<Command> commands = new ArrayList<Command>();
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final String bindName = "command";
    
    /**
     * Defines a command which can be executed through the in-game chat.
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     *
     */
    public class Command {
        @SerializedName("controller")
        private String controller;
        
        private CommandController controllerInstance;
        
        @SerializedName("premission")
        private int premission = -1;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("parameter")
        private String[] parameter;
        
        @SerializedName("shortDescription")
        private String shortDescription;
        
        @SerializedName("longDescription")
        private String longDescription;
        
        @SerializedName("examples")
        private String[] examples;
        
        public String getController() {
            return controller;
        }
        
        public int getPremission() {
            return premission;
        }
        
        public String getName() {
            return name;
        }
        
        public String[] getParameter() {
            return parameter;
        }

        public String getShortDescription() {
            return shortDescription;
        }
        
        public String getLongDescription() {
            return longDescription;
        }
        
        public String[] getExamples() {
            return examples;
        }
        
        public String getUseMessage() {
            String use = name;
            
            for (String param : parameter) {
                use += " <" + param + ">";
            }
            
            return use;
        }
        
        /**
         * Load the controller object. If a similar controller object  already exists in the controllers map, it will use the existing one.
         * If no instance of this class exist, it will try to create one.
         * @return
         */
        public boolean loadController(Map<String, CommandController> controllers) {
            //check if object of the controller already exists
            if (controllers.containsKey(controller)) {
                controllerInstance = controllers.get(controller);
                
                return true;
            }
            //load controller object
            else {
                try {
                    //load the class
                    Class<?> c = Class.forName(controller);
                    
                    //create an instance
                    Object instance = c.newInstance();
                    if (instance instanceof CommandController) {
                        controllerInstance = (CommandController) instance;
                        controllers.put(controller, controllerInstance);
                    }
                    
                    return true;
                    
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            
            return false;
        }
        
        /**
         * Verify that a method with the command name exists in the controller class. It doesn't verify overloaded methods or the parameters in any way.
         * @return Returns true if a method with the command name exists.
         */
        public boolean methodExists() {
            Method[] methods = controllerInstance.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Execute the method which is assigned to the command.
         * @param param 
         */
        public void methodExecute(String cmd) {
            
            Binding bind = new Binding();
            bind.setVariable(bindName, controllerInstance);
            GroovyShell shell = new GroovyShell(bind);

            shell.evaluate(cmd);
        }
    }
    
    /**
     * Get the only instance of this class.
     * @return Returns the only instance of this class.
     */
    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        
        return instance;
    }
    
    /**
     * Create an instance of the CommandManager which will load all commands.
     */
    private CommandManager() {
        loadCommands();
    }
    
    /**
     * Load all default and mod commands from the JSON files.
     */
    private void loadCommands() {
        //Load default commands
        try {
            String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "chat" + File.separator + "commands.json";
            FileReader reader = new FileReader(helpFile);
            
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray Jarray = parser.parse(reader).getAsJsonArray();

            Command cmd;
            for (JsonElement obj : Jarray)
            {
                cmd = gson.fromJson(obj , Command.class);
                //validate the JSON content
                if (!cmd.getController().isEmpty() && cmd.getPremission() != -1 && !cmd.getName().isEmpty()) {
                    //load the controller object
                    if (cmd.loadController(controllers)) {
                        //verify that a method with the command name exists in the controller class
                        if (cmd.methodExists()) {
                            commands.add(cmd);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        //=============================
        // TODO load mod commands here
        //=============================
        
        //sort commands by there names
        Comparator<Command> comp = new Comparator<CommandManager.Command>() {
            @Override
            public int compare(Command o1, Command o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        
        Collections.sort(commands, comp);
    }
    
    /**
     * Execute a command.
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
        
        if (paramsCount == 0) {
            paramsStr = "()";
        }
        
        //get the command
        Command cmd = getCommand(commandName);
        
        //check if the command is loaded
        if (cmd == null) {
            ChatManager.getInstance().addMessage("Unknown command '" + commandName + "'");
            
            return false;
        }
        
        //verify the number of parameters
        if (paramsCount > cmd.getParameter().length) {
            ChatManager.getInstance().addMessage(cmd.getUseMessage());
            ChatManager.getInstance().addMessage("To many parameters for command '" + commandName + "'");
            
            return false;
        }
        
        String executeString = bindName + "." + cmd.getName() + " " + paramsStr;
        logger.log(Level.INFO, "Execute command '" + executeString + "'");
        
        try {
            //execute the command
            cmd.methodExecute(executeString);
            
            return true;
        } catch (Exception e) {
            //TODO better error handling and error message
            ChatManager.getInstance().addMessage(cmd.getUseMessage());
            ChatManager.getInstance().addMessage("Error executing command '" + commandName + "'.");
            e.printStackTrace();
            
            return false;
        }
    }
    
    /**
     * Get a command by its name.
     * @param name The name of the command.
     * @return Returns the command or null if no command with the given name was loaded.
     */
    public Command getCommand(String name) {
        for (Command cmd : commands) {
            if (cmd.getName().equals(name)) {
                return cmd;
            }
        }
        
        return null;
    }
    
    /**
     * Get the list of all loaded commands.
     * @return Returns the command list.
     */
    public List<Command> getCommandList() {
        return commands;
    }
}
