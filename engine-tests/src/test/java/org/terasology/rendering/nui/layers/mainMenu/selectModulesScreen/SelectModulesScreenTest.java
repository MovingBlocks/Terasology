package org.terasology.rendering.nui.layers.mainMenu.selectModulesScreen;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.config.flexible.FlexibleConfigManager;
import org.terasology.config.flexible.FlexibleConfigManagerImpl;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.world.generator.internal.WorldGeneratorManager;

public class SelectModulesScreenTest {

	private SelectModulesScreen sut;

	@BeforeClass
	public static void initClass() {
		Context context = new ContextImpl();
		Config config = new Config(context);
		config.loadDefaults();
		context.put(FlexibleConfigManager.class, new FlexibleConfigManagerImpl());
		CoreRegistry.setContext(context);
		CoreRegistry.put(Config.class, config);
		CoreRegistry.put(ModuleManager.class, new ModuleManagerImpl(config));
		CoreRegistry.put(WorldGeneratorManager.class, new WorldGeneratorManager(context));
	}

	@Before
	public void init() {
		sut = new SelectModulesScreen();
		InjectionHelper.inject(sut);
		sut.initialise();
	}

	@Test
	public void givenInitializedModScreen_whenScreenOnOpenedCalled_thenVerifySuccess() {
		sut.onOpened();
	}

	@Test
	public void givenInitializedAndOpenedModScreen_whenScreenOnClosedCalled_thenVerifySuccess() {
		sut.onOpened();
		sut.onClosed();
	}
}
