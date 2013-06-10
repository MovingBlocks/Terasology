package org.terasology.input;

import org.lwjgl.input.Keyboard;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;

/**
 * @author Immortius
 */
@RegisterSystem
public class BindCommands implements ComponentSystem {

    @In
    private InputSystem inputSystem;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Maps a key to a function")
    public String bindKey(@CommandParam("key") String key, @CommandParam("function") String bind) {
        inputSystem.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
        StringBuilder builder = new StringBuilder();
        builder.append("Mapped ").append(Keyboard.getKeyName(Keyboard.getKeyIndex(key))).append(" to action ");
        builder.append(bind);
        return builder.toString();
    }

    @Command(shortDescription = "Switches to typical key binds for AZERTY")
    public String AZERTY() {
        inputSystem.linkBindButtonToKey(Keyboard.KEY_Z, "engine:forwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_S, "engine:backwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_Q, "engine:left");

        return "Changed key bindings to AZERTY keyboard layout.";
    }

    @Command(shortDescription = "Switches to typical key binds for NEO 2 keyboard layout")
    public String NEO() {
        inputSystem.linkBindButtonToKey(Keyboard.KEY_V, "engine:forwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_I, "engine:backwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_U, "engine:left");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_A, "engine:right");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_L, "engine:useItem");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_G, "engine:inventory");

        return "Changed key bindings to NEO 2 keyboard layout.";
    }
}
