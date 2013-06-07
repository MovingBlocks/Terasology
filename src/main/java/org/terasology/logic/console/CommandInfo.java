package org.terasology.logic.console;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Metadata on a command, including the ability to execute it.
 *
 * @author Immortius
 */
public class CommandInfo {
    private static final Logger logger = LoggerFactory.getLogger(CommandInfo.class);

    private static final String BIND_CONTEXT = "command";

    private Object provider;

    private String name;
    private String[] parameterNames;
    private String shortDescription;
    private String helpText;

    public CommandInfo(Method method, Object provider) {
        this.provider = provider;
        this.name = method.getName();
        this.parameterNames = new String[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            parameterNames[i] = method.getParameterTypes()[i].toString();
            for (Annotation paramAnnot : method.getParameterAnnotations()[i]) {
                if (paramAnnot instanceof CommandParam) {
                    parameterNames[i] = ((CommandParam) paramAnnot).name();
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
     * @returns A message if this command returns a message
     */
    public String execute(String params) {
        Binding bind = new Binding();
        bind.setVariable(BIND_CONTEXT, provider);
        GroovyShell shell = new GroovyShell(bind);

        logger.debug("Executing command {}.{}({})", BIND_CONTEXT, name, params);
        Object result = shell.evaluate(BIND_CONTEXT + "." + name + "(" + params + ")");
        if (result != null) {
            return result.toString();
        }
        return "";
    }
}
