package org.terasology.logic.manager;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 29/04/12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class GroovyHelp {

    private String commandName;
    private String[] parameters;
    private String commandDesc;
    private String commandHelp;
    private String[] examples;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String getCommandDesc() {
        return commandDesc;
    }

    public void setCommandDesc(String commandDesc) {
        this.commandDesc = commandDesc;
    }

    public String getCommandHelp() {
        return commandHelp;
    }

    public void setCommandHelp(String commandHelp) {
        this.commandHelp = commandHelp;
    }

    public String[] getExamples() {
        return examples;
    }

    public void setExamples(String[] examples) {
        this.examples = examples;
    }


}
