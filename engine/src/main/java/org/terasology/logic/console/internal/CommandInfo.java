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
package org.terasology.logic.console.internal;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Metadata on a command, including the ability to execute it.
 *
 * @author Immortius
 */
public class CommandInfo {
    private static final Logger logger = LoggerFactory.getLogger(CommandInfo.class);

    private static final Joiner PARAM_JOINER = Joiner.on(", ");
    private static final String PROVIDER_VAR = "provider";
    private static final String CLIENT_VAR = "client";

    private Method method;
    private Object provider;

    private String name;
    private List<String> parameterNames = Lists.newArrayList();
    private String shortDescription;
    private String helpText;
    private boolean clientEntityRequired;
    private boolean runOnServer;

    public CommandInfo(Method method, Object provider) {
        this.method = method;
        this.provider = provider;
        this.name = method.getName();
        Command commandAnnotation = method.getAnnotation(Command.class);
        if (commandAnnotation == null) {
            throw new IllegalArgumentException("Method not annotated with command");
        }
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            Class<?> parameterType = method.getParameterTypes()[i];
            if (i == method.getParameterTypes().length - 1 && parameterType == EntityRef.class) {
                clientEntityRequired = true;
            } else {
                String paramName = method.getParameterTypes()[i].toString();
                for (Annotation paramAnnot : method.getParameterAnnotations()[i]) {
                    if (paramAnnot instanceof CommandParam) {
                        paramName = ((CommandParam) paramAnnot).value();
                        break;
                    }
                }
                parameterNames.add(paramName);
            }
        }
        this.runOnServer = commandAnnotation.runOnServer();
        this.shortDescription = commandAnnotation.shortDescription();
        this.helpText = commandAnnotation.helpText();
    }

    public String getName() {
        return name;
    }

    public Collection<String> getParameterNames() {
        return ImmutableList.copyOf(parameterNames);
    }

    public int getParameterCount() {
        return parameterNames.size();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getHelpText() {
        return helpText;
    }

    public boolean isClientEntityRequired() {
        return clientEntityRequired;
    }

    public boolean isRunOnServer() {
        return runOnServer;
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
     * @param params
     * @param callingClient
     * @return
     */
    public String execute(List<String> params, EntityRef callingClient) {
        Object[] processedParams = new Object[method.getParameterTypes().length];
        if (isClientEntityRequired()) {
            if (params.size() + 1 != method.getParameterTypes().length) {
                return "Incorrect number of parameters, expected " + (method.getParameterTypes().length - 1);
            }

            processedParams[processedParams.length - 1] = callingClient;
        } else if (params.size() != method.getParameterTypes().length) {
            return "Incorrect number of parameters, expected " + (method.getParameterTypes().length);
        }
        for (int i = 0; i < params.size(); ++i) {
            Class<?> type = method.getParameterTypes()[i];
            if (type == Float.TYPE) {
                try {
                    processedParams[i] = Float.parseFloat(params.get(i));
                } catch (NumberFormatException e) {
                    return "Bad argument '" + params.get(i) + "' - " + e.getMessage();
                }
            } else if (type == Integer.TYPE) {
                try {
                    processedParams[i] = Integer.parseInt(params.get(i));
                } catch (NumberFormatException e) {
                    return "Bad argument '" + params.get(i) + "' - " + e.getMessage();
                }
            } else if (type == String.class) {
                String value = params.get(i);
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                processedParams[i] = value;
            }
        }
        try {
            Object result = method.invoke(provider, processedParams);
            return (result != null) ? result.toString() : "";
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Error running command {} with parameters {}", name, params, e);
            return "Error running command: " + e.getMessage();
        }
    }
}
