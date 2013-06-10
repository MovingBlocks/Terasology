package org.terasology.logic.console;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.utilities.StringConstants;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem
public class ConsoleSystem implements ComponentSystem{

    @In
    private Console console;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "General help", helpText = "Prints out short descriptions for all available commands.")
    public String help() {
        StringBuilder msg = new StringBuilder();
        List<CommandInfo> commands = console.getCommandList();
        for (CommandInfo cmd : commands) {
            if (!msg.toString().isEmpty()) {
                msg.append("\n");
            }
            msg.append(cmd.getUsageMessage()).append(" - ").append(cmd.getShortDescription());
        }
        return msg.toString();
    }

    @Command(shortDescription = "Detailed help on a command")
    public String help(@CommandParam("command") String command) {
        Collection<CommandInfo> cmdCollection = console.getCommand(command);
        if (cmdCollection.isEmpty()) {
            return "No help available for command '" + command + "'. Unknown command.";
        } else {
            StringBuilder msg = new StringBuilder();

            for (CommandInfo cmd : cmdCollection) {
                msg.append("=====================================================================================================================");
                msg.append(StringConstants.NEW_LINE);
                msg.append(cmd.getUsageMessage());
                msg.append(StringConstants.NEW_LINE);
                msg.append("=====================================================================================================================");
                msg.append(StringConstants.NEW_LINE);
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(StringConstants.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(StringConstants.NEW_LINE);
                } else if (!cmd.getShortDescription().isEmpty()) {
                    msg.append(cmd.getShortDescription());
                    msg.append(StringConstants.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(StringConstants.NEW_LINE);
                }
                msg.append(StringConstants.NEW_LINE);
            }
            return msg.toString();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            console.addMessage(event.getFormattedMessage());
        }
    }

    @ReceiveEvent(components = ClientComponent.class, netFilter = RegisterMode.AUTHORITY)
    public void onCommand(CommandEvent event, EntityRef entity) {
        List<String> params = console.splitParameters(event.getParams());
        for (CommandInfo cmd : console.getCommand(event.getCommand())) {
            if (cmd.getParameterCount() == params.size() && cmd.isRunOnServer()) {
                console.execute(event.getCommand() + " " + event.getParams(), entity);
                break;
            }
        }
    }
}
