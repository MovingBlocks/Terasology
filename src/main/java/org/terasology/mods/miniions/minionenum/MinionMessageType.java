package org.terasology.mods.miniions.minionenum;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 28/05/12
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
public enum MinionMessageType {
    NoPath,
    Idle;

    public String[] getMessage(MinionMessageType messages) {
        switch (messages) {
            case NoPath: {
                String[] content = new String[3];
                content[0] = "Command not executed"; //description
                content[1] = "No Path found"; //Title
                content[2] = "The minion couldn't find a path to destination"; //body
                return content;
            }
            case Idle: {
                String[] content = new String[3];
                content[0] = "No more commands"; //description
                content[1] = "iddle minion"; //Title
                content[2] = "The minion has no more commands to execute and is now iddle"; //body
                return content;
            }
            default:
                return null;
        }
    }
}


