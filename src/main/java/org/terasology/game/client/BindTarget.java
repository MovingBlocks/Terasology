package org.terasology.game.client;

public class BindTarget {
    private String modName;
    private String description;

    public BindTarget(String modName, String description) {
        this.modName = modName;
        this.description = description;
    }

    public void process(ButtonState state) {
        switch (state) {
            case DOWN: {
                start();
                break;
            }
            case UP: {
                end();
                break;
            }
            case REPEAT: {
                repeat();
                break;
            }
        }
    }

    public void start() {
    }

    public void repeat() {
    }

    public void end() {
    }

    public String getDescription() {
        return description;
    }

    public String getModName() {
        return modName;
    }
}
