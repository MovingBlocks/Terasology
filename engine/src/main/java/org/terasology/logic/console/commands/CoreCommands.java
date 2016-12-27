/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.console.commands;

import com.google.common.collect.Ordering;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.Time;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.i18n.TranslationProject;
import org.terasology.i18n.TranslationSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.console.suggesters.CommandNameSuggester;
import org.terasology.logic.console.suggesters.ScreenSuggester;
import org.terasology.logic.console.suggesters.SkinSuggester;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.Direction;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.naming.Name;
import org.terasology.network.ClientComponent;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.Server;
import org.terasology.persistence.WorldDumper;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.editor.layers.NUIEditorScreen;
import org.terasology.rendering.nui.editor.layers.NUISkinEditorScreen;
import org.terasology.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.loader.BlockFamilyDefinition;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 */
@RegisterSystem
public class CoreCommands extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private PrefabManager prefabManager;

    @In
    private BlockManager blockManager;

    @In
    private Console console;

    @In
    private Time time;

    @In
    private GameEngine gameEngine;

    @In
    private NetworkSystem networkSystem;

    @In
    private DisplayDevice displayDevice;

    @In
    private NUIManager nuiManager;

    @In
    private NUIEditorSystem nuiEditorSystem;

    @In
    private NUISkinEditorSystem nuiSkinEditorSystem;

    @In
    private AssetManager assetManager;

    @In
    private TranslationSystem translationSystem;

    @In
    private Config config;

    /**
     * Search commands/prefabs/assets with matching name, description, help text, usage or required permission
     * @param searched String which is used to search for match
     * @return String containing result of search
     */
    @Command(shortDescription = "Search commands/prefabs/assets",
             helpText = "Displays commands, prefabs, and assets with matching name, description, "
                 + "help text, usage or required permission")
    public String search(@CommandParam("searched") String searched) {
        String searchLowercase = searched.toLowerCase();

        List<String> commands = findCommandMatches(searchLowercase);
        List<String> prefabs = findPrefabMatches(searchLowercase);
        List<String> blocks = findBlockMatches(searchLowercase);

        // String containing numbers of commands, prefabs and block that match searched string
        String result = "Found " + commands.size() + " command matches, " + prefabs.size() +
            " prefab matches and " + blocks.size() + " block matches when searching for '" + searched + "'.";

        // iterate through commands adding them to result
        if (commands.size() > 0) {
            result += "\nCommands:";
            result = commands.stream().reduce(result, (t, u) -> t + "\n    " + u);
        }

        // iterate through prefabs adding them to result
        if (prefabs.size() > 0) {
            result += "\nPrefabs:";
            result = prefabs.stream().reduce(result, (t, u) -> t + "\n    " + u);
        }

        // iterate through blocks adding them to result
        if (blocks.size() > 0) {
            result += "\nBlocks:";
            result = blocks.stream().reduce(result, (t, u) -> t + "\n    " + u);
        }

        return result;
    }

    /**
     * List commands that match searched string
     * @param searchLowercase searched string lowercase
     * @return List of commands that match searched string
     */
    private List<String> findCommandMatches(String searchLowercase) {
        return console.getCommands().stream().filter(command -> matchesSearch(searchLowercase, command))
            .map(ConsoleCommand::getUsage).collect(Collectors.toList());
    }

    /**
     * Determine if command is matching one of criteria
     * @param searchLowercase searched string
     * @param command ConsoleCommand to check if matches searched string
     * @return boolean containing true if command matches searched string else false
     */
    private static boolean matchesSearch(String searchLowercase, ConsoleCommand command) {
        return command.getName().toLowerCase().contains(searchLowercase)
            || command.getDescription().toLowerCase().contains(searchLowercase)
            || command.getHelpText().toLowerCase().contains(searchLowercase)
            || command.getUsage().toLowerCase().contains(searchLowercase)
            || command.getRequiredPermission().toLowerCase().contains(searchLowercase);
    }

    /**
     * List prefabs that match searched string
     * @param searchLowercase searched string
     * @return List of prefabs that match searched string
     */
    private List<String> findPrefabMatches(String searchLowercase) {
        return StreamSupport.stream(prefabManager.listPrefabs().spliterator(), false)
            .filter(prefab -> matchesSearch(searchLowercase, prefab))
            .map(prefab -> prefab.getUrn().toString()).collect(Collectors.toList());
    }

    /**
     * Determine if prefab is matching one of criteria
     * @param searchLowercase searched String
     * @param prefab Prefab to check if matches searched string
     * @return boolean containing true if prefab matches searched string else false
     */
    private static boolean matchesSearch(String searchLowercase, Prefab prefab) {
        return prefab.getName().toLowerCase().contains(searchLowercase)
            || prefab.getUrn().toString().toLowerCase().contains(searchLowercase);
    }

    /**
     * List blocks that match searched string
     * @param searchLowercase searched string
     * @return List of blocks that match searched string
     */
    private List<String> findBlockMatches(String searchLowercase) {
        return assetManager.getAvailableAssets(BlockFamilyDefinition.class)
            .stream().<Optional<BlockFamilyDefinition>>map(urn -> assetManager.getAsset(urn, BlockFamilyDefinition.class))
            .filter(def -> def.isPresent() && def.get().isLoadable() && matchesSearch(searchLowercase, def.get()))
            .map(r -> new BlockUri(r.get().getUrn()).toString()).collect(Collectors.toList());
    }

    /**
     * Determine if block family matches one of criteria
     * @param searchLowercase searched string
     * @param def BlockFamilyDefinition to be checked
     * @return boolean containing true if blockFamilyDefinition matches searched string else false
     */
    private static boolean matchesSearch(String searchLowercase, BlockFamilyDefinition def) {
        return def.getUrn().toString().toLowerCase().contains(searchLowercase);
    }

    /**
     * Time dilation slows down the passage of time by affecting how the main game loop runs,
     * with the goal being to handle high-latency situations by spreading out processing over a longer amount of time
     * @param rate float time dilation
     */
    @Command(shortDescription = "Alter the rate of time")
    public void setTimeDilation(@CommandParam("dilation") float rate) {
        time.setGameTimeDilation(rate);
    }

    /**
     * Change the UI language
     * @param langTag String containing language code to change
     * @return String containing language or if not recognized error message
     */
    @Command(shortDescription = "Changes the UI language")
    public String setLanguage(@CommandParam("language-tag") String langTag) {
        Locale locale = Locale.forLanguageTag(langTag);
        TranslationProject proj = translationSystem.getProject(new SimpleUri("engine:menu"));

        // Try if language exists
        if (proj.getAvailableLocales().contains(locale)) {
            config.getSystem().setLocale(locale);
            nuiManager.invalidate();
            String nat = translationSystem.translate("${engine:menu#this-language-native}", locale);
            String eng = translationSystem.translate("${engine:menu#this-language-English}", locale);
            return String.format("Language set to %s (%s)", nat, eng);
        } else {
            return "Unrecognized locale! Try one of: " + proj.getAvailableLocales();
        }
    }

    /**
     * Shows a ui screen
     * @param uri String containing ui screen name
     * @return String containing Success if UI was change or Not found if screen is missing
     */
    @Command(shortDescription = "Shows a ui screen", helpText = "Can be used for debugging/testing, example: \"showScreen migTestScreen\"")
    public String showScreen(@CommandParam(value = "uri", suggester = ScreenSuggester.class) String uri) {
        return nuiManager.pushScreen(uri) != null ? "Success" : "Not found";
    }

    /**
     * Reloads ui screen
     * @param ui String containing ui screen name
     * @return String containing Success if UI was reloaded or No unique resource found if more screens were found
     */
    @Command(shortDescription = "Reloads a ui screen")
    public String reloadScreen(@CommandParam("ui") String ui) {
        Set<ResourceUrn> urns = assetManager.resolve(ui, UIElement.class);
        if (urns.size() == 1) {
            ResourceUrn urn = urns.iterator().next();
            boolean wasOpen = nuiManager.isOpen(urn);
            if (wasOpen) {
                nuiManager.closeScreen(urn);
            }

            if (wasOpen) {
                nuiManager.pushScreen(urn);
            }
            return "Success";
        } else {
            return "No unique resource found";
        }
    }

    /**
     * Opens the NUI editor for a ui screen
     * @param uri String containing ui screen name
     * @return String containing final message
     */
    @Command(shortDescription = "Opens the NUI editor for a ui screen", requiredPermission = PermissionManager.NO_PERMISSION)
    public String editScreen(@CommandParam(value = "uri", suggester = ScreenSuggester.class) String uri) {
        if (!nuiEditorSystem.isEditorActive()) {
            nuiEditorSystem.toggleEditor();
        }
        Set<ResourceUrn> urns = assetManager.resolve(uri, UIElement.class);
        switch (urns.size()) {
            case 0:
                return String.format("No asset found for screen '%s'", uri);
            case 1:
                ResourceUrn urn = urns.iterator().next();
                ((NUIEditorScreen) nuiManager.getScreen(NUIEditorScreen.ASSET_URI)).selectAsset(urn);
                return "Success";
            default:
                return String.format("Multiple matches for screen '%s': {%s}", uri, Arrays.toString(urns.toArray()));
        }
    }

    /**
     * Opens the NUI editor for a ui skin
     * @param uri String containing name of ui skin
     * @return String containing final message
     */
    @Command(shortDescription = "Opens the NUI editor for a ui skin", requiredPermission = PermissionManager.NO_PERMISSION)
    public String editSkin(@CommandParam(value = "uri", suggester = SkinSuggester.class) String uri) {
        if (!nuiSkinEditorSystem.isEditorActive()) {
            nuiSkinEditorSystem.toggleEditor();
        }
        Set<ResourceUrn> urns = assetManager.resolve(uri, UISkin.class);
        switch (urns.size()) {
            case 0:
                return String.format("No asset found for screen '%s'", uri);
            case 1:
                ResourceUrn urn = urns.iterator().next();
                ((NUISkinEditorScreen) nuiManager.getScreen(NUISkinEditorScreen.ASSET_URI)).selectAsset(urn);
                return "Success";
            default:
                return String.format("Multiple matches for screen '%s': {%s}", uri, Arrays.toString(urns.toArray()));
        }
    }

    /**
     * Switches to fullscreen or to windowed mode
     * @return String containing final message
     */
    @Command(shortDescription = "Toggles Fullscreen Mode", requiredPermission = PermissionManager.NO_PERMISSION)
    public String fullscreen() {
        displayDevice.setFullscreen(!displayDevice.isFullscreen());
        if (displayDevice.isFullscreen()) {
            return "Switched to fullscreen mode";
        } else {
            return "Switched to windowed mode";
        }

    }

    /**
     * Removes all entities of the given prefab
     * @param prefabName String containing prefab name
     */
    @Command(shortDescription = "Removes all entities of the given prefab", runOnServer = true)
    public void destroyEntitiesUsingPrefab(@CommandParam("prefabName") String prefabName) {
        Prefab prefab = entityManager.getPrefabManager().getPrefab(prefabName);
        if (prefab != null) {
            for (EntityRef entity : entityManager.getAllEntities()) {
                if (prefab.equals(entity.getParentPrefab())) {
                    entity.destroy();
                }
            }
        }
    }

    /**
     * Triggers a graceful shutdown of the game after the current frame, attempting to dispose all game resources
     */
    @Command(shortDescription = "Exits the game", requiredPermission = PermissionManager.NO_PERMISSION)
    public void exit() {
        gameEngine.shutdown();
    }

    /**
     * Join a game
     * @param address String containing address of game server
     * @param portParam Integer containing game server port
     */
    @Command(shortDescription = "Join a game", requiredPermission = PermissionManager.NO_PERMISSION)
    public void join(@CommandParam("address") final String address, @CommandParam(value = "port", required = false) Integer portParam) {
        final int port = portParam != null ? portParam : TerasologyConstants.DEFAULT_PORT;

        Callable<JoinStatus> operation = () -> networkSystem.join(address, port);

        final WaitPopup<JoinStatus> popup = nuiManager.pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Join Game", "Connecting to '" + address + ":" + port + "' - please wait ...");
        popup.onSuccess(result -> {
            if (result.getStatus() != JoinStatus.Status.FAILED) {
                gameEngine.changeState(new StateLoading(result));
            } else {
                MessagePopup screen = nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                screen.setMessage("Failed to Join", "Could not connect to server - " + result.getErrorMessage());
            }
        });
        popup.startOperation(operation, true);
    }

    /**
     * Leaves the current game and returns to main menu
     * @return String containing final message
     */
    @Command(shortDescription = "Leaves the current game and returns to main menu",
             requiredPermission = PermissionManager.NO_PERMISSION)
    public String leave() {
        if (networkSystem.getMode() != NetworkMode.NONE) {
            gameEngine.changeState(new StateMainMenu());
            return "Leaving..";
        } else {
            return "Not connected";
        }
    }

    /**
     * Writes out information on all entities to a text file for debugging
     * @throws IOException thrown when error with saving file occures
     */
    @Command(shortDescription = "Writes out information on all entities to a text file for debugging",
             helpText = "Writes entity information out into a file named \"entityDump.txt\".")
    public void dumpEntities() throws IOException {
        EngineEntityManager engineEntityManager = (EngineEntityManager) entityManager;
        PrefabSerializer prefabSerializer = new PrefabSerializer(engineEntityManager.getComponentLibrary(), engineEntityManager.getTypeSerializerLibrary());
        WorldDumper worldDumper = new WorldDumper(engineEntityManager, prefabSerializer);
        worldDumper.save(PathManager.getInstance().getHomePath().resolve("entityDump.txt"));
    }

    /**
     * Spawns an instance of a prefab in the world
     * @param sender Sender of command
     * @param prefabName String containing prefab name
     * @return String containing final message
     */
    @Command(shortDescription = "Spawns an instance of a prefab in the world", runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String spawnPrefab(@Sender EntityRef sender, @CommandParam("prefabId") String prefabName) {
        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        LocationComponent characterLocation = clientComponent.character.getComponent(LocationComponent.class);


        Vector3f spawnPos = characterLocation.getWorldPosition();
        Vector3f offset = new Vector3f(characterLocation.getWorldDirection());
        offset.scale(2);
        spawnPos.add(offset);
        Vector3f dir = new Vector3f(characterLocation.getWorldDirection());
        dir.y = 0;
        if (dir.lengthSquared() > 0.001f) {
            dir.normalize();
        } else {
            dir.set(Direction.FORWARD.getVector3f());
        }
        Quat4f rotation = Quat4f.shortestArcQuat(Direction.FORWARD.getVector3f(), dir);

        Optional<Prefab> prefab = Assets.getPrefab(prefabName);
        if (prefab.isPresent() && prefab.get().getComponent(LocationComponent.class) != null) {
            entityManager.create(prefab.get(), spawnPos, rotation);
            return "Done";
        } else if (!prefab.isPresent()) {
            return "Unknown prefab";
        } else {
            return "Prefab cannot be spawned (no location component)";
        }
    }

    /**
     * Spawns a block in front of the player
     * @param sender Sender of command
     * @param blockName String containing name of block to spawn
     * @return String containg final message
     */
    @Command(shortDescription = "Spawns a block in front of the player", helpText = "Spawns the specified block as a " +
        "item in front of the player. You can simply pick it up.", runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String spawnBlock(@Sender EntityRef sender, @CommandParam("blockName") String blockName) {
        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        LocationComponent characterLocation = clientComponent.character.getComponent(LocationComponent.class);

        Vector3f spawnPos = characterLocation.getWorldPosition();
        Vector3f offset = characterLocation.getWorldDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily block = blockManager.getBlockFamily(blockName);
        if (block == null) {
            return "";
        }

        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
        EntityRef blockItem = blockItemFactory.newInstance(block);

        blockItem.send(new DropItemEvent(spawnPos));
        return "Spawned block.";
    }

    /**
     * Your ping to the server
     * @param sender Sender of command
     * @return String containing ping or error message
     */
    @Command(shortDescription = "Your ping to the server", helpText = "The time it takes the packet " +
        "to reach the server and back", requiredPermission = PermissionManager.NO_PERMISSION)
    public String ping(@Sender EntityRef sender) {
        Server server = networkSystem.getServer();
        if (server == null) {
            //TODO: i18n
            return "Please make sure you are connected to an online server (singleplayer doesn't count)";
        }
        String[] remoteAddress = server.getRemoteAddress().split("-");
        String address = remoteAddress[1];
        int port = Integer.valueOf(remoteAddress[2]);
        try {
            Instant starts = Instant.now();
            Socket sock = new Socket(address, port);
            Instant ends = Instant.now();
            sock.close();
            long delay = Duration.between(starts, ends).toMillis();
            return String.format("%d ms", delay);
        } catch (UnknownHostException e) {
            return String.format("Error: Unknown host \"%s\" at %s:%s -- %s", remoteAddress[0], remoteAddress[1], remoteAddress[2], e);
        } catch (IOException e) {
            return String.format("Error: Failed to ping server \"%s\" at %s:%s -- %s", remoteAddress[0], remoteAddress[1], remoteAddress[2], e);
        }
    }


    /**
     * Prints out short descriptions for all available commands, or a longer help text if a command is provided.
     * @param commandName String containing command for which will be displayed help
     * @return String containing short description of all commands or longer help text if command is provided
     */
    @Command(shortDescription = "Prints out short descriptions for all available commands, or a longer help text if a command is provided.",
             requiredPermission = PermissionManager.NO_PERMISSION)
    public String help(@CommandParam(value = "command", required = false, suggester = CommandNameSuggester.class) Name commandName) {
        if (commandName == null) {
            StringBuilder msg = new StringBuilder();
            // Get all commands, with appropriate sorting
            List<ConsoleCommand> commands = Ordering.natural().immutableSortedCopy(console.getCommands());

            for (ConsoleCommand cmd : commands) {
                if (!msg.toString().isEmpty()) {
                    msg.append(Console.NEW_LINE);
                }

                msg.append(FontColor.getColored(cmd.getUsage(), ConsoleColors.COMMAND));
                msg.append(" - ");
                msg.append(cmd.getDescription());
            }

            return msg.toString();
        } else {
            ConsoleCommand cmd = console.getCommand(commandName);
            if (cmd == null) {
                return "No help available for command '" + commandName + "'. Unknown command.";
            } else {
                StringBuilder msg = new StringBuilder();

                msg.append("=====================================================================================================================");
                msg.append(Console.NEW_LINE);
                msg.append(cmd.getUsage());
                msg.append(Console.NEW_LINE);
                msg.append("=====================================================================================================================");
                msg.append(Console.NEW_LINE);
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(Console.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(Console.NEW_LINE);
                } else if (!cmd.getDescription().isEmpty()) {
                    msg.append(cmd.getDescription());
                    msg.append(Console.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(Console.NEW_LINE);
                }

                return msg.toString();
            }
        }
    }

    /**
     * Clears the console window of previous messages.
     */
    @Command(shortDescription = "Clears the console window of previous messages.", requiredPermission = PermissionManager.NO_PERMISSION)
    public void clear() {
        console.clear();
    }
}
