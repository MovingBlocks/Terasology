
package org.terasology.engine.subsystem.config;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.BindsConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindableAxis;
import org.terasology.input.BindableButton;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.input.RegisterBindAxis;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.RegisterRealBindAxis;
import org.terasology.input.events.AxisEvent;
import org.terasology.input.events.ButtonEvent;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;

import java.lang.annotation.Annotation;
import java.util.List;

public class BindsSubsystem implements EngineSubsystem, BindsManager {

private static final Logger logger = LoggerFactory.getLogger(BindsSubsystem.class);
    private BindsConfig binds = new BindsConfig();
    
    @Override
    public String getName() {
        return "Binds";
    }

    @Override
    public void preInitialise(Context rootContext) {
        loadBindsConfig();
        rootContext.put(BindsManager.class, this);
    }
    
    /**
     * @return A new BindsConfig, with inputs set from the DefaultBinding annotations on bind classes
     */
    @Override
    public BindsConfig createDefault(Context context) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        BindsConfig config = new BindsConfig();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            if (moduleManager.getRegistry().getLatestModuleVersion(moduleId).isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        FromModule filter = new FromModule(environment, moduleId);
                        Iterable<Class<?>> buttons = environment.getTypesAnnotatedWith(RegisterBindButton.class, filter);
                        Iterable<Class<?>> axes = environment.getTypesAnnotatedWith(RegisterRealBindAxis.class, filter);
                        addButtonDefaultsFor(moduleId, buttons,config);
                        addAxisDefaultsFor(moduleId, axes,config);
                    }
                }
            }
        }
        return config;
    }

    /**
     * Updates a config with any binds that it may be missing, through reflection over RegisterBindButton annotations
     */
    @Override
    public void updateForChangedMods(Context context) {
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
                        updateButtonInputsFor(moduleId, buttons);
                        updateAxisInputsFor(moduleId, axes);
                    }
                }
            }
        }
    }

    private void updateButtonInputsFor(Name moduleId, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                SimpleUri bindUri = new SimpleUri(moduleId, info.id());
                if (!binds.hasBinds(bindUri)) {
                    addBind(moduleId, buttonEvent, info.id(),binds);
                }
            }
        }
    }

    private void updateAxisInputsFor(Name moduleId, Iterable<Class<?>> classes) {
        for (Class<?> axisEvent : classes) {
            if (AxisEvent.class.isAssignableFrom(axisEvent)) {
                RegisterRealBindAxis info = axisEvent.getAnnotation(RegisterRealBindAxis.class);
                SimpleUri bindUri = new SimpleUri(moduleId, info.id());
                if (!binds.hasBinds(bindUri)) {
                    addBind(moduleId, axisEvent, info.id(),binds);
                }
            }
        }
    }

    private void addButtonDefaultsFor(Name moduleId, Iterable<Class<?>> classes,BindsConfig config) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                addBind(moduleId, buttonEvent, info.id(),config);
            }
        }
    }

    private void addAxisDefaultsFor(Name moduleId, Iterable<Class<?>> classes,BindsConfig config) {
        for (Class<?> axisEvent : classes) {
            if (AxisEvent.class.isAssignableFrom(axisEvent)) {
                RegisterRealBindAxis info = axisEvent.getAnnotation(RegisterRealBindAxis.class);
                addBind(moduleId, axisEvent, info.id(),config);
            }
        }
    }

    private void addBind(Name moduleName, Class<?> event, String id,BindsConfig config) {
        List<Input> defaultInputs = Lists.newArrayList();
        for (Annotation annotation : event.getAnnotationsByType(DefaultBinding.class)) {
            DefaultBinding defaultBinding = (DefaultBinding) annotation;
            Input input = defaultBinding.type().getInput(defaultBinding.id());
            if (!config.values().contains(input)) {
                defaultInputs.add(input);
            }
        }
        SimpleUri bindUri = new SimpleUri(moduleName, id);
        config.setBinds(bindUri, defaultInputs);
    }

    @Override
    public void applyBinds(InputSystem inputSystem, ModuleManager moduleManager) {
        inputSystem.clearBinds();
        ModuleEnvironment env = moduleManager.getEnvironment();
        registerButtonBinds(inputSystem, env, env.getTypesAnnotatedWith(RegisterBindButton.class));
        registerAxisBinds(inputSystem, env, env.getTypesAnnotatedWith(RegisterBindAxis.class));
        registerRealAxisBinds(inputSystem, env, env.getTypesAnnotatedWith(RegisterRealBindAxis.class));
    }

    private void registerAxisBinds(InputSystem inputSystem, ModuleEnvironment environment, Iterable<Class<?>> classes) {
        for (Class<?> registerBindClass : classes) {
            RegisterBindAxis info = registerBindClass.getAnnotation(RegisterBindAxis.class);
            Name moduleId = environment.getModuleProviding(registerBindClass);
            SimpleUri id = new SimpleUri(moduleId, info.id());
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                BindableButton positiveButton = inputSystem.getBindButton(new SimpleUri(info.positiveButton()));
                BindableButton negativeButton = inputSystem.getBindButton(new SimpleUri(info.negativeButton()));
                if (positiveButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing positive button \"{}\"", id, info.positiveButton());
                    continue;
                }
                if (negativeButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing negative button \"{}\"", id, info.negativeButton());
                    continue;
                }
                try {
                    BindableAxis bindAxis = inputSystem.registerBindAxis(id.toString(), (BindAxisEvent) registerBindClass.newInstance(), positiveButton, negativeButton);
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

    private void registerRealAxisBinds(InputSystem inputSystem, ModuleEnvironment environment, Iterable<Class<?>> classes) {
        for (Class<?> registerBindClass : classes) {
            RegisterRealBindAxis info = registerBindClass.getAnnotation(RegisterRealBindAxis.class);
            Name moduleId = environment.getModuleProviding(registerBindClass);
            SimpleUri id = new SimpleUri(moduleId, info.id());
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindAxisEvent instance = (BindAxisEvent) registerBindClass.newInstance();
                    BindableAxis bindAxis = inputSystem.registerRealBindAxis(id.toString(), instance);
                    bindAxis.setSendEventMode(info.eventMode());
                    for (Input input : binds.getBinds(id)) {
                        inputSystem.linkAxisToInput(input, id);
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

    private void registerButtonBinds(InputSystem inputSystem, ModuleEnvironment environment, Iterable<Class<?>> classes) {
        for (Class<?> registerBindClass : classes) {
            RegisterBindButton info = registerBindClass.getAnnotation(RegisterBindButton.class);
            SimpleUri bindUri = new SimpleUri(environment.getModuleProviding(registerBindClass), info.id());
            if (BindButtonEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindableButton bindButton = inputSystem.registerBindButton(bindUri, info.description(), (BindButtonEvent) registerBindClass.newInstance());
                    bindButton.setMode(info.mode());
                    bindButton.setRepeating(info.repeating());

                    binds.getBinds(bindUri).stream().filter(input -> input != null).forEach(input ->
                            inputSystem.linkBindButtonToInput(input, bindUri));

                    logger.debug("Registered button bind: {}", bindUri);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register button bind \"{}\"", e);
                }
            } else {
                logger.error("Failed to register button bind \"{}\", does not extend BindButtonEvent", bindUri);
            }
        }
    }

    @Override
    public BindsConfig getBindsConfig() {
        return binds;
    }

    private void loadBindsConfig() {
     // TODO separate save logic when moving to flexible config
        logger.warn("Binds loading is not implemented yet");
    }

    @Override
    public void saveBindsConfig() {
        // TODO separate save logic when moving to flexible config
        logger.warn("Binds saving is not implemented yet");
    }
}
