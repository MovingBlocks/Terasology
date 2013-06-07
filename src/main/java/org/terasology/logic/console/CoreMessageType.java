package org.terasology.logic.console;


import org.newdawn.slick.Color;

/**
 * @author Immortius
 */
public enum CoreMessageType implements MessageType {
    CONSOLE(Color.black),
    CHAT(Color.green);

    private Color color;

    private CoreMessageType(Color color) {
        this.color = new Color(color);
    }

    public Color getColor() {
        return color;
    }
}
