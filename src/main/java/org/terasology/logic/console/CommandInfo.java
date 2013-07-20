/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.logic.console;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;

import java.lang.annotation.Annotation;
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

    private Object provider;

    private String name;
    private List<String> parameterNames = Lists.newArrayList();
    private String shortDescription;
    private String helpText;
    private boolean clientEntityRequired;
    private boolean runOnServer;

    public CommandInfo(Method method, Object provider) {
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
    public String execute(String params, EntityRef callingClient) {
        Binding bind = new Binding();
        bind.setVariable(PROVIDER_VAR, provider);
        if (isClientEntityRequired()) {
            bind.setVariable(CLIENT_VAR, callingClient);
        }
        GroovyShell shell = new GroovyShell(bind);

        Object result;
        if (isClientEntityRequired()) {
            String fullParams = (params.trim().isEmpty()) ? CLIENT_VAR : PARAM_JOINER.join(params, CLIENT_VAR);
            logger.debug("Executing command {}.{}({})", PROVIDER_VAR, name, fullParams);
            result = shell.evaluate(PROVIDER_VAR + "." + name + "(" + fullParams + ")");
        } else {
            logger.debug("Executing command {}.{}({})", PROVIDER_VAR, name, params);
            result = shell.evaluate(PROVIDER_VAR + "." + name + "(" + params + ")");
        }
        return (result != null) ? result.toString() : "";
    }
}
