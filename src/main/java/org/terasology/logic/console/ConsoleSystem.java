package org.terasology.logic.console;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius
 */
@RegisterSystem
public class ConsoleSystem implements ComponentSystem{

    @In
    private Console console;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            console.addMessage(event.getFormattedMessage());
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onCommand(CommandEvent event, EntityRef entity) {
        String[] params = event.getParams().split(Console.PARAM_SPLIT_REGEX);
        for (CommandInfo cmd : console.getCommand(event.getCommand())) {
            if (cmd.getParameterCount() == params.length && cmd.isRunOnServer()) {
                console.execute(event.getCommand() + " " + event.getParams(), entity);
                break;
            }
        }
    }
}
