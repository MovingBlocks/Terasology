// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commands;

import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import org.joml.Vector3f;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.i18n.TranslationProject;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleColors;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.console.suggesters.CommandNameSuggester;
import org.terasology.engine.logic.console.suggesters.ScreenSuggester;
import org.terasology.engine.logic.console.suggesters.SkinSuggester;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.math.Direction;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.JoinStatus;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.PingService;
import org.terasology.engine.network.Server;
import org.terasology.engine.persistence.WorldDumper;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.editor.layers.NUIEditorScreen;
import org.terasology.engine.rendering.nui.editor.layers.NUISkinEditorScreen;
import org.terasology.engine.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.engine.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.FontColor;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.skin.UISkinAsset;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Adds a series of useful commands to the game. Likely these could be moved to more fitting places over time.
 */
@RegisterSystem
public class CoreCommands extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

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
    private SystemConfig systemConfig;

    @In
    private ModuleManager moduleManager;

    /**
     * Determine if command is matching one of criteria
     *
     * @param searchLowercase searched string
     * @param command         ConsoleCommand to check if matches searched string
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
     * Determine if prefab is matching one of criteria
     *
     * @param searchLowercase searched String
     * @param prefab          Prefab to check if matches searched string
     * @return boolean containing true if prefab matches searched string else false
     */
    private static boolean matchesSearch(String searchLowercase, Prefab prefab) {
        return prefab.getName().toLowerCase().contains(searchLowercase)
                || prefab.getUrn().toString().toLowerCase().contains(searchLowercase);
    }

    /**
     * Determine if block family matches one of criteria
     *
     * @param searchLowercase searched string
     * @param def             BlockFamilyDefinition to be checked
     * @return boolean containing true if blockFamilyDefinition matches searched string else false
     */
    private static boolean matchesSearch(String searchLowercase, BlockFamilyDefinition def) {
        return def.getUrn().toString().toLowerCase().contains(searchLowercase);
    }

    /**
     * Search commands/prefabs/assets with matching name, description, help text, usage or required permission
     *
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
        if (!commands.isEmpty()) {
            result += "\nCommands:";
            result = commands.stream().reduce(result, (t, u) -> t + "\n    " + u);
        }

        // iterate through prefabs adding them to result
        if (!prefabs.isEmpty()) {
            result += "\nPrefabs:";
            result = prefabs.stream().reduce(result, (t, u) -> t + "\n    " + u);
        }

        // iterate through blocks adding them to result
        if (!blocks.isEmpty()) {
            result += "\nBlocks:";
            result = blocks.stream().reduce(result, (t, u) -> t + "\n    " + u);
        }

        return result;
    }

    /**
     * List commands that match searched string
     *
     * @param searchLowercase searched string lowercase
     * @return List of commands that match searched string
     */
    private List<String> findCommandMatches(String searchLowercase) {
        return console.getCommands().stream().filter(command -> matchesSearch(searchLowercase, command))
                .map(ConsoleCommand::getUsage).collect(Collectors.toList());
    }

    /**
     * List prefabs that match searched string
     *
     * @param searchLowercase searched string
     * @return List of prefabs that match searched string
     */
    private List<String> findPrefabMatches(String searchLowercase) {
        return StreamSupport.stream(prefabManager.listPrefabs().spliterator(), false)
                .filter(prefab -> matchesSearch(searchLowercase, prefab))
                .map(prefab -> prefab.getUrn().toString()).collect(Collectors.toList());
    }

    /**
     * List blocks that match searched string
     *
     * @param searchLowercase searched string
     * @return List of blocks that match searched string
     */
    private List<String> findBlockMatches(String searchLowercase) {
        ResourceUrn curUrn;
        List<String> outputList = new ArrayList<>();
        for (ResourceUrn urn : assetManager.getAvailableAssets(BlockFamilyDefinition.class)) {
            // Current urn for logging purposes to find the broken urn
            curUrn = urn;
            try {
                Optional<BlockFamilyDefinition> def = assetManager.getAsset(urn, BlockFamilyDefinition.class);
                if (def.isPresent() && def.get().isLoadable() && matchesSearch(searchLowercase, def.get())) {
                    outputList.add(new BlockUri(def.get().getUrn()).toString());
                }
            } catch (Exception e) {  // If a prefab is broken , it will throw an exception
                console.addMessage("Note : Search may not return results if invalid assets are present");
                console.addMessage("Error parsing : " + curUrn.toString());
                console.addMessage(e.toString());
            }
        }
        return outputList;

    }

    /**
     * Time dilation slows down the passage of time by affecting how the main game loop runs,
     * with the goal being to handle high-latency situations by spreading out processing over a longer amount of time
     *
     * @param rate float time dilation
     */
    @Command(shortDescription = "Alter the rate of time")
    public void setTimeDilation(@CommandParam("dilation") float rate) {
        time.setGameTimeDilation(rate);
    }

    /**
     * Change the UI language
     *
     * @param langTag String containing language code to change
     * @return String containing language or if not recognized error message
     */
    @Command(shortDescription = "Changes the UI language")
    public String setLanguage(@CommandParam("language-tag") String langTag) {
        Locale locale = Locale.forLanguageTag(langTag);
        TranslationProject proj = translationSystem.getProject(new ResourceUrn("engine:menu"));

        // Try if language exists
        if (proj.getAvailableLocales().contains(locale)) {
            systemConfig.locale.set(locale);
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
     *
     * @param uri String containing ui screen name
     * @return String containing Success if UI was change or Not found if screen is missing
     */
    @Command(shortDescription = "Shows a ui screen", helpText = "Can be used for debugging/testing, example: \"showScreen migTestScreen\"")
    public String showScreen(@CommandParam(value = "uri", suggester = ScreenSuggester.class) String uri) {
        return nuiManager.pushScreen(uri) != null ? "Success" : "Not found";
    }

    /**
     * Reloads ui screen
     *
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
     *
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
     *
     * @param uri String containing name of ui skin
     * @return String containing final message
     */
    @Command(shortDescription = "Opens the NUI editor for a ui skin", requiredPermission = PermissionManager.NO_PERMISSION)
    public String editSkin(@CommandParam(value = "uri", suggester = SkinSuggester.class) String uri) {
        if (!nuiSkinEditorSystem.isEditorActive()) {
            nuiSkinEditorSystem.toggleEditor();
        }
        Set<ResourceUrn> urns = assetManager.resolve(uri, UISkinAsset.class);
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
     *
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
     *
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
     *
     * @param address   String containing address of game server
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
     *
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
     * Writes out information on entities having specific components to a text file for debugging
     * If no component names provided - writes out information on all entities
     *
     * @param componentNames string contains one or several component names, if more then one name
     *                       provided - they must be braced with double quotes and all names separated
     *                       by space
     * @return String containing information about number of entities saved
     * @throws IOException thrown when error with saving file occures
     */
    @Command(shortDescription = "Writes out information on all entities to a JSON file for debugging",
            helpText = "Writes entity information out into a file named \"<timestamp>-entityDump.json\"." +
                    " Supports list of component names, which will be used to only save entities that contains" +
                    " one or more of those components. Names should be separated by spaces.")
    public String dumpEntities(@CommandParam(value = "componentNames", required = false) String... componentNames) throws IOException {
        int savedEntityCount;
        EngineEntityManager engineEntityManager = (EngineEntityManager) entityManager;
        PrefabSerializer prefabSerializer =
                new PrefabSerializer(engineEntityManager.getComponentLibrary(), engineEntityManager.getTypeSerializerLibrary());
        WorldDumper worldDumper = new WorldDumper(engineEntityManager, prefabSerializer);
        Path outFile = PathManager.getInstance().getHomePath().resolve(Instant.now().toString().replaceAll(":", "-") + "-entityDump.json");
        if (componentNames.length == 0) {
            savedEntityCount = worldDumper.save(outFile);
        } else {
            List<Class<? extends Component>> filterComponents = Arrays.stream(componentNames)
                    .map(String::trim) //Trim off whitespace
                    .filter(o -> !o.isEmpty()) //Remove empty strings
                    .map(o -> o.toLowerCase().endsWith("component") ? o : o + "component") //All component class names end with "component"
                    .map(o -> Streams.stream(moduleManager.getEnvironment().getSubtypesOf(Component.class))
                            .filter(e -> e.getSimpleName().equalsIgnoreCase(o))
                            .findFirst())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (!filterComponents.isEmpty()) {
                savedEntityCount = worldDumper.save(outFile, filterComponents);
            } else {
                return "Could not find components matching given names";
            }
        }
        return "Number of entities saved: " + savedEntityCount;
    }

    /**
     * Spawns an instance of a prefab in the world
     *
     * @param sender     Sender of command
     * @param prefabName String containing prefab name
     * @return String containing final message
     */
    @Command(shortDescription = "Spawns an instance of a prefab in the world", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String spawnPrefab(@Sender EntityRef sender, @CommandParam("prefabId") String prefabName) {
        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        LocationComponent characterLocation = clientComponent.character.getComponent(LocationComponent.class);


        Vector3f spawnPos = characterLocation.getWorldPosition(new Vector3f());
        Vector3f offset = characterLocation.getWorldDirection(new Vector3f());
        offset.mul(2);
        spawnPos.add(offset);
        Vector3f dir = characterLocation.getWorldDirection(new Vector3f());
        dir.y = 0;
        if (dir.lengthSquared() > 0.001f) {
            dir.normalize();
        } else {
            dir.set(Direction.FORWARD.asVector3f());
        }

        return Assets.getPrefab(prefabName).map(prefab -> {
            LocationComponent loc = prefab.getComponent(LocationComponent.class);
            if (loc != null) {
                entityManager.create(prefab, spawnPos);
                return "Done";
            } else {
                return "Prefab cannot be spawned (no location component)";
            }
        }).orElse("Unknown prefab");
    }

    /**
     * Spawns a block in front of the player
     *
     * @param sender    Sender of command
     * @param blockName String containing name of block to spawn
     * @return String containg final message
     */
    @Command(shortDescription = "Spawns a block in front of the player", helpText = "Spawns the specified block as a " +
            "item in front of the player. You can simply pick it up.", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String spawnBlock(@Sender EntityRef sender, @CommandParam("blockName") String blockName) {
        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        LocationComponent characterLocation = clientComponent.character.getComponent(LocationComponent.class);

        Vector3f spawnPos = characterLocation.getWorldPosition(new Vector3f());
        Vector3f offset = characterLocation.getWorldDirection(new Vector3f());
        offset.mul(3);
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

    @Command(shortDescription = "Mass-drops the desired block however many times the player indicates",
            helpText = "First parameter indicates which block to drop, second parameter how many",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String bulkDrop(@Sender EntityRef sender, @CommandParam("blockName") String blockName, @CommandParam("value") int value) {

        //This is a loop which gives the particular amount of block the player wants to spawn
        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        LocationComponent characterLocation = clientComponent.character.getComponent(LocationComponent.class);

        Vector3f spawnPos = characterLocation.getWorldPosition(new Vector3f());
        Vector3f offset = characterLocation.getWorldDirection(new Vector3f());

        offset.mul(3);
        spawnPos.add(5, 10, 0);
        BlockFamily block = blockManager.getBlockFamily(blockName);
        if (block == null) {
            return "Sorry, your block is not found";
        }

        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
        if (value > 5000) {
            return "Value exceeds the maximum limit of 5000 blocks. your value: " + value + " blocks";
        }

        for (int i = 0; i < value; i++) {

            EntityRef blockItem = blockItemFactory.newInstance(block);
            blockItem.send(new DropItemEvent(spawnPos));
        }

        // this returns the block you have spawned and the amount
        return "Dropped " + value + " " + blockName + " Blocks :)";
    }

    @Command(shortDescription = "Sets up a typical bowling pin arrangement in front of the player. ",
            helpText = "Spawns the specific block in a regular bowling pin pattern, Throw something at it!",
            runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String bowlingPrep(@Sender EntityRef sender, @CommandParam("blockName") String blockName) {

        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        LocationComponent characterLocation = clientComponent.character.getComponent(LocationComponent.class);

        Vector3f spawnPos = characterLocation.getWorldPosition(new Vector3f());
        Vector3f offset = characterLocation.getWorldDirection(new Vector3f());
        offset.mul(5);
        spawnPos.add(offset);
        BlockFamily block = blockManager.getBlockFamily(blockName);
        if (block == null) {
            return "Sorry, your block is not found";
        }

        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
        Vector3f startPos = new Vector3f(spawnPos);

        float deltax = 0.5f; // delta x is the distance between the pins in the rows.
        float deltaz = 1.0f; //delta z is the distance between the rows.
        float vectorY = 0.0f; //the height of the drop (to be modified to keep the bowlingPin upright)
        //rownumber loop is for selecting row
        for (int rownumber = 0; rownumber < 4; rownumber++) {
            startPos.add(deltax * (4 - rownumber), vectorY, deltaz); //Spawn starting position for Rownumber
            // pinPosx loop is for vectorx position of bowling pin  in  a particular row
            for (int pinPosx = 0; pinPosx <= rownumber; pinPosx++) {
                EntityRef blockItem = blockItemFactory.newInstance(block);
                blockItem.send(new DropItemEvent(startPos));
                if (pinPosx < rownumber) {
                    startPos.add(2 * deltax, 0, 0); // drift of position in vector x coordinate, for the last pin stop drifting
                }
            }
            startPos.add(-deltax * (rownumber + 4), 0, 0); // returns to start position
        }
        return "prepared 10 " + blockName + " in a bowling pin pattern :)";
    }

    /**
     * Your ping to the server
     *
     * @param sender Sender of command
     * @return String containing ping or error message
     */
    @Command(shortDescription = "Your ping to the server", helpText = "The time it takes the packet " +
            "to reach the server and back", requiredPermission = PermissionManager.NO_PERMISSION)
    public String ping(@Sender EntityRef sender) {
        Server server = networkSystem.getServer();
        if (server == null) {
            //TODO: i18n
            if (networkSystem.getMode().isServer()) {
                return "Your player is running on the server";
            } else {
                return "Please make sure you are connected to an online server (singleplayer doesn't count)";
            }
        }
        String[] remoteAddress = server.getRemoteAddress().split("-");
        String address = remoteAddress[1];
        int port = Integer.parseInt(remoteAddress[2]);
        try {
            PingService pingService = new PingService(address, port);
            long delay = pingService.call();
            return String.format("%d ms", delay);
        } catch (UnknownHostException e) {
            return String.format("Error: Unknown host \"%s\" at %s:%s -- %s", remoteAddress[0], remoteAddress[1], remoteAddress[2], e);
        } catch (IOException e) {
            return String.format("Error: Failed to ping server \"%s\" at %s:%s -- %s", remoteAddress[0], remoteAddress[1], remoteAddress[2], e);
        }
    }

    /**
     * Prints out short descriptions for all available commands, or a longer help text if a command is provided.
     *
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

                msg.append("===========================================================================================================");
                msg.append(Console.NEW_LINE);
                msg.append(cmd.getUsage());
                msg.append(Console.NEW_LINE);
                msg.append("===========================================================================================================");
                msg.append(Console.NEW_LINE);
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(Console.NEW_LINE);
                    msg.append("===========================================================================================================");
                    msg.append(Console.NEW_LINE);
                } else if (!cmd.getDescription().isEmpty()) {
                    msg.append(cmd.getDescription());
                    msg.append(Console.NEW_LINE);
                    msg.append("===========================================================================================================");
                    msg.append(Console.NEW_LINE);
                }
                if (!cmd.getRequiredPermission().isEmpty()) {
                    msg.append("Required permission level - " + cmd.getRequiredPermission());
                    msg.append(Console.NEW_LINE);
                    msg.append("===========================================================================================================");
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
