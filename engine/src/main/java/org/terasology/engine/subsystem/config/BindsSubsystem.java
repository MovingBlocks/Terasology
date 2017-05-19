
package org.terasology.engine.subsystem.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindableAxis;
import org.terasology.input.BindableButton;
import org.terasology.input.ControllerInput;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.MouseInput;
import org.terasology.input.RegisterBindAxis;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.RegisterRealBindAxis;
import org.terasology.input.events.AxisEvent;
import org.terasology.input.events.ButtonEvent;
import org.terasology.input.internal.AbstractBindableAxis;
import org.terasology.input.internal.BindableAxisImpl;
import org.terasology.input.internal.BindableButtonImpl;
import org.terasology.input.internal.BindableRealAxis;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BindsSubsystem implements EngineSubsystem, BindsManager {

    private static final Logger logger = LoggerFactory.getLogger(BindsSubsystem.class);
    private BindsConfig bindsConfig = new BindsConfig();
    private BindsConfig defaultBindsConfig = new BindsConfig();
    private Map<SimpleUri, BindableButton> buttonLookup = Maps.newHashMap();
    private List<BindableButton> buttonBinds = Lists.newArrayList();
    private Context context;
    private Map<String, BindableRealAxis> axisLookup = Maps.newHashMap();
    private List<AbstractBindableAxis> axisBinds = Lists.newArrayList();

    // Links between primitive inputs and bind buttons
    private Map<Integer, BindableButton> keyBinds = Maps.newHashMap();
    private Map<MouseInput, BindableButton> mouseButtonBinds = Maps.newHashMap();
    private Map<ControllerInput, BindableButton> controllerBinds = Maps.newHashMap();
    private Map<Input, BindableRealAxis> controllerAxisBinds = Maps.newHashMap();
    private BindableButton mouseWheelUpBind;
    private BindableButton mouseWheelDownBind;

    @Override
    public BindableButton getMouseWheelUpBind() {
        return mouseWheelUpBind;
    }

    @Override
    public BindableButton getMouseWheelDownBind() {
        return mouseWheelDownBind;
    }

    @Override
    public Map<MouseInput, BindableButton> getMouseButtonBinds() {
        return mouseButtonBinds;
    }

    @Override
    public List<AbstractBindableAxis> getAxisBinds() {
        return axisBinds;
    }

    @Override
    public Map<Input, BindableRealAxis> getControllerAxisBinds() {
        return controllerAxisBinds;
    }

    @Override
    public Map<ControllerInput, BindableButton> getControllerBinds() {
        return controllerBinds;
    }

    @Override
    public Map<Integer, BindableButton> getKeyBinds() {
        return keyBinds;
    }

    @Override
    public String getName() {
        return "Binds";
    }

    @Override
    public List<BindableButton> getButtonBinds() {
        return buttonBinds;
    }

    @Override
    public void preInitialise(Context context) {
        this.context = context;
        loadBindsConfig();
        context.put(BindsManager.class, this);
    }

    @Override
    public BindsConfig getDefaultBindsConfig() {
        BindsConfig copy = new BindsConfig();
        //SimpleUri and Input are immutable, no need for a deep copy
        copy.setBinds(defaultBindsConfig);
        return copy;
    }

    @Override
    public void updateConfigWithDefaultBinds() {
        //default bindings are overridden
        defaultBindsConfig = new BindsConfig();
        updateDefaultBinds(context, defaultBindsConfig);
        //actual bindings may be actualized
        updateDefaultBinds(context, bindsConfig);
    }

    private void updateDefaultBinds(Context context, BindsConfig config) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            if (moduleManager.getRegistry().getLatestModuleVersion(moduleId).isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        FromModule filter = new FromModule(environment, moduleId);
                        Iterable<Class<?>> buttons = environment.getTypesAnnotatedWith(RegisterBindButton.class, filter);
                        Iterable<Class<?>> axes = environment.getTypesAnnotatedWith(RegisterRealBindAxis.class, filter);
                        addButtonDefaultsFor(moduleId, buttons, config);
                        addAxisDefaultsFor(moduleId, axes, config);
                    }
                }
            }
        }
    }

    private void addButtonDefaultsFor(Name moduleId, Iterable<Class<?>> classes, BindsConfig config) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                SimpleUri bindUri = new SimpleUri(moduleId, info.id());
                if (!config.hasBinds(bindUri)) {
                    addDefaultBindings(bindUri, buttonEvent, config);
                }
            }
        }
    }

    private void addAxisDefaultsFor(Name moduleId, Iterable<Class<?>> classes, BindsConfig config) {
        for (Class<?> axisEvent : classes) {
            if (AxisEvent.class.isAssignableFrom(axisEvent)) {
                RegisterRealBindAxis info = axisEvent.getAnnotation(RegisterRealBindAxis.class);
                SimpleUri bindUri = new SimpleUri(moduleId, info.id());
                if (!config.hasBinds(bindUri)) {
                    addDefaultBindings(bindUri, axisEvent, config);
                }
            }
        }
    }

    private void addDefaultBindings(SimpleUri bindUri, Class<?> event, BindsConfig config) {
        List<Input> defaultInputs = fetchDefaultBindings(event, config);
        config.setBinds(bindUri, defaultInputs);
    }

    private List<Input> fetchDefaultBindings(Class<?> event, BindsConfig config) {
        List<Input> defaultInputs = Lists.newArrayList();
        Collection<Input> values = config.values();
        for (Annotation annotation : event.getAnnotationsByType(DefaultBinding.class)) {
            DefaultBinding defaultBinding = (DefaultBinding) annotation;
            Input input = defaultBinding.type().getInput(defaultBinding.id());
            if (!values.contains(input)) {
                defaultInputs.add(input);
            } else {
                logger.warn("Input {} is already registered, can not use it for event {}", input, event);
            }
        }
        return defaultInputs;
    }

    @Override
    public void registerBinds() {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        ModuleEnvironment environment = moduleManager.getEnvironment();
        clearBinds();
        registerButtonBinds(environment);
        registerAxisBinds(environment);
        registerRealAxisBinds(environment);
    }

    private void clearBinds() {
        buttonLookup.clear();
        buttonBinds.clear();
        axisLookup.clear();
        axisBinds.clear();
        keyBinds.clear();
        controllerBinds.clear();
        controllerAxisBinds.clear();
        mouseButtonBinds.clear();
        mouseWheelUpBind = null;
        mouseWheelDownBind = null;
    }

    private void registerButtonBinds(ModuleEnvironment environment) {
        Iterable<Class<?>> classes = environment.getTypesAnnotatedWith(RegisterBindButton.class);
        for (Class<?> registerBindClass : classes) {
            RegisterBindButton info = registerBindClass.getAnnotation(RegisterBindButton.class);
            SimpleUri bindUri = new SimpleUri(environment.getModuleProviding(registerBindClass), info.id());
            if (BindButtonEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindableButton bindButton = registerBindButton(bindUri, info.description(), (BindButtonEvent) registerBindClass.newInstance());
                    bindButton.setMode(info.mode());
                    bindButton.setRepeating(info.repeating());

                    bindsConfig.getBinds(bindUri).stream().filter(input -> input != null).forEach(input -> linkBindButtonToInput(input, bindUri));

                    logger.debug("Registered button bind: {}", bindUri);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register button bind \"{}\"", e);
                }
            } else {
                logger.error("Failed to register button bind \"{}\", does not extend BindButtonEvent", bindUri);
            }
        }
    }

    private void registerAxisBinds(ModuleEnvironment environment) {
        Iterable<Class<?>> classes = environment.getTypesAnnotatedWith(RegisterBindAxis.class);
        for (Class<?> registerBindClass : classes) {
            RegisterBindAxis info = registerBindClass.getAnnotation(RegisterBindAxis.class);
            Name moduleId = environment.getModuleProviding(registerBindClass);
            SimpleUri id = new SimpleUri(moduleId, info.id());
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                BindableButton positiveButton = getBindButton(new SimpleUri(info.positiveButton()));
                BindableButton negativeButton = getBindButton(new SimpleUri(info.negativeButton()));
                if (positiveButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing positive button \"{}\"", id, info.positiveButton());
                    continue;
                }
                if (negativeButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing negative button \"{}\"", id, info.negativeButton());
                    continue;
                }
                try {
                    BindableAxis bindAxis = registerBindAxis(id.toString(), (BindAxisEvent) registerBindClass.newInstance(), positiveButton, negativeButton);
                    bindAxis.setSendEventMode(info.eventMode());
                    logger.debug("Registered axis bind: {}", id);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register axis bind \"{}\"", id, e);
                }
            } else {
                logger.error("Failed to register axis bind \"{}\", does not extend BindAxisEvent", id);
            }
        }
    }

    private void registerRealAxisBinds(ModuleEnvironment environment) {
        Iterable<Class<?>> classes = environment.getTypesAnnotatedWith(RegisterRealBindAxis.class);
        for (Class<?> registerBindClass : classes) {
            RegisterRealBindAxis info = registerBindClass.getAnnotation(RegisterRealBindAxis.class);
            Name moduleId = environment.getModuleProviding(registerBindClass);
            SimpleUri id = new SimpleUri(moduleId, info.id());
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindAxisEvent instance = (BindAxisEvent) registerBindClass.newInstance();
                    BindableAxis bindAxis = registerRealBindAxis(id.toString(), instance);
                    bindAxis.setSendEventMode(info.eventMode());
                    for (Input input : bindsConfig.getBinds(id)) {
                        linkAxisToInput(input, id);
                    }
                    logger.debug("Registered axis bind: {}", id);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register axis bind \"{}\"", id, e);
                }
            } else {
                logger.error("Failed to register axis bind \"{}\", does not extend BindAxisEvent", id);
            }
        }
    }

    private void linkAxisToInput(Input input, SimpleUri bindId) {
        BindableRealAxis bindInfo = axisLookup.get(bindId.toString());
        controllerAxisBinds.put(input, bindInfo);
    }

    @Override
    public void linkBindButtonToKey(int key, SimpleUri bindId) {
        BindableButton bindInfo = buttonLookup.get(bindId);
        keyBinds.put(key, bindInfo);
    }

    private void linkBindButtonToMouse(MouseInput mouseButton, SimpleUri bindId) {
        BindableButton bindInfo = buttonLookup.get(bindId);
        mouseButtonBinds.put(mouseButton, bindInfo);
    }

    private void linkBindButtonToMouseWheel(int direction, SimpleUri bindId) {
        if (direction > 0) {
            mouseWheelDownBind = buttonLookup.get(bindId);
        } else if (direction < 0) {
            mouseWheelUpBind = buttonLookup.get(bindId);
        }
    }

    private void linkBindButtonToController(ControllerInput button, SimpleUri bindId) {
        BindableButton bindInfo = buttonLookup.get(bindId);
        controllerBinds.put(button, bindInfo);
    }

    private void linkBindButtonToInput(Input input, SimpleUri bindId) {
        switch (input.getType()) {
            case KEY:
                linkBindButtonToKey(input.getId(), bindId);
                break;
            case MOUSE_BUTTON:
                MouseInput button = MouseInput.find(input.getType(), input.getId());
                linkBindButtonToMouse(button, bindId);
                break;
            case MOUSE_WHEEL:
                linkBindButtonToMouseWheel(input.getId(), bindId);
                break;
            case CONTROLLER_BUTTON:
                linkBindButtonToController((ControllerInput) input, bindId);
                break;
            default:
                break;
        }
    }

    private BindableButton getBindButton(SimpleUri bindId) {
        return buttonLookup.get(bindId);
    }

    private BindableAxis registerBindAxis(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        BindableAxisImpl axis = new BindableAxisImpl(id, event, positiveButton, negativeButton);
        axisBinds.add(axis);
        return axis;
    }

    private BindableAxis registerRealBindAxis(String id, BindAxisEvent event) {
        BindableRealAxis axis = new BindableRealAxis(id.toString(), event);
        axisBinds.add(axis);
        axisLookup.put(id, axis);
        return axis;
    }

    private BindableButton registerBindButton(SimpleUri bindId, String displayName, BindButtonEvent event) {
        BindableButtonImpl bind = new BindableButtonImpl(bindId, displayName, event);
        buttonLookup.put(bindId, bind);
        buttonBinds.add(bind);
        return bind;
    }

    @Override
    public BindsConfig getBindsConfig() {
        return bindsConfig;
    }

    @Override
    public void loadBindsConfig() {
        Optional<JsonObject> configJson = loadFileToJson(getConfigPath());
        if (configJson.isPresent()) {
            bindsConfig = Config.createGson().fromJson(configJson.get(), BindsConfig.class);
        }
    }

    @Override
    public void shutdown() {
        saveBindsConfig();
    }

    @Override
    public void saveBindsConfig() {
        //TODO replace with flexible config
        Path bindsConfiPath = getConfigPath();
        try (BufferedWriter writer = Files.newBufferedWriter(bindsConfiPath, TerasologyConstants.CHARSET)) {
            Config.createGson().toJson(bindsConfig, writer);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    private Path getConfigPath() {
        return PathManager.getInstance().getHomePath().resolve("bindsConfig.cfg");
    }

    private Optional<JsonObject> loadFileToJson(Path configPath) {
        if (Files.isRegularFile(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, TerasologyConstants.CHARSET)) {
                JsonElement userConfig = new JsonParser().parse(reader);
                if (userConfig.isJsonObject()) {
                    return Optional.of(userConfig.getAsJsonObject());
                }
            } catch (IOException e) {
                logger.error("Failed to load config file {}");
            }
        }
        return Optional.empty();
    }
}
