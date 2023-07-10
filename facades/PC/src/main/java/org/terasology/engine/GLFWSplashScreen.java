// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.terasology.engine.core.GameScheduler;
import org.terasology.engine.core.TerasologyEngineStatus;
import org.terasology.splash.SplashScreen;
import org.terasology.splash.glfw.graphics.Color;
import org.terasology.splash.glfw.graphics.Renderer;
import org.terasology.splash.glfw.graphics.Texture;
import org.terasology.splash.glfw.graphics.Window;
import org.terasology.splash.glfw.widgets.ActivatableImage;
import org.terasology.splash.glfw.widgets.AnimatedBoxRow;
import org.terasology.splash.glfw.widgets.BorderedRectangle;
import org.terasology.splash.glfw.widgets.Image;
import org.terasology.splash.glfw.widgets.Widget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class GLFWSplashScreen implements SplashScreen, Runnable {
    private final List<Widget> widgets = new LinkedList<>();
    private Texture pixel;
    private Window window;
    private String message = "Loading...";
    private boolean isClosing;


    @Override
    public void post(String msg) {
        this.message = msg;
        widgets.stream()
                .filter(w -> w instanceof ActivatableImage)
                .map(w -> (ActivatableImage) w)
                .forEach(i -> i.post(msg));
    }

    @Override
    public void close() {
        if (window != null) {
            isClosing = true;
        }
    }

    @Override
    public void run() {
        Renderer renderer = Mono.just(0)
                .subscribeOn(GameScheduler.graphics())
                .doOnNext(n -> initWindow())
                .map((n) -> new Renderer())
                .doOnNext(Renderer::init)
                .doOnNext(r ->
                    GL11.glClearColor(0f, 0f, 0f, 0f))
                .block();

        final long contextId = GameScheduler.runBlockingGraphics("steal context", GLFW::glfwGetCurrentContext);


        Flux.interval(Duration.ofMillis(100))
                .onBackpressureDrop()
                .map(n -> GLFW.glfwGetTime())
                .buffer(2, 1)
                .map(times -> times.get(1) - times.get(0))
                .publishOn(GameScheduler.graphics())
                .takeUntil(dTime -> isClosing || window.isClosing())
                .doOnNext(dTime -> {
                    GLFW.glfwMakeContextCurrent(contextId);
                    renderer.clear();
                    widgets.forEach(widget -> widget.update(dTime));
                    widgets.forEach(i -> i.render(renderer));
                    renderer.drawText(message, 30, 25, Color.BLACK);
                    window.update();
                    GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
                })
                .doFinally(type -> {
                    GLFW.glfwMakeContextCurrent(contextId);
                    widgets.stream()
                            .filter(w -> w instanceof Image)
                            .map(w -> (Image) w)
                            .forEach(Image::delete);
                    pixel.delete();
                    renderer.dispose();
                    window.destroy();
                    GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
                }).subscribe();
    }

    private void initWindow() {
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Cannot init GLFW!");
        }
        int width = 800;
        int height = 289;
        window = new Window(width, height, "", false);

        pixel = new Texture();
        ByteBuffer bytes = BufferUtils.createByteBuffer(4);
        bytes.put((byte) -1).put((byte) -1).put((byte) -1).put((byte) -1);
        bytes.flip();
        pixel.bind();
        pixel.uploadData(1, 1, bytes);

        try {
            widgets.add(new Image(GLFWSplashScreen.class.getResource("/splash/splash.png"), 0, 0));
            widgets.add(new ActivatableImage(
                    GLFWSplashScreen.class.getResource("/splash/splash_1.png"),
                    0, 0,
                    TerasologyEngineStatus.PREPARING_SUBSYSTEMS.getDescription()));
            widgets.add(new ActivatableImage(
                    GLFWSplashScreen.class.getResource("/splash/splash_2.png"),
                    150, 0,
                    TerasologyEngineStatus.INITIALIZING_MODULE_MANAGER.getDescription()));
            widgets.add(new ActivatableImage(
                    GLFWSplashScreen.class.getResource("/splash/splash_3.png"),
                    300, 0,
                    TerasologyEngineStatus.INITIALIZING_ASSET_TYPES.getDescription()));
            widgets.add(new ActivatableImage(
                    GLFWSplashScreen.class.getResource("/splash/splash_4.png"),
                    450, 0,
                    TerasologyEngineStatus.INITIALIZING_SUBSYSTEMS.getDescription()));
            widgets.add(new ActivatableImage(
                    GLFWSplashScreen.class.getResource("/splash/splash_5.png"),
                    630, 0,
                    TerasologyEngineStatus.INITIALIZING_ASSET_MANAGEMENT.getDescription()));
            widgets.add(new Image(GLFWSplashScreen.class.getResource("/splash/splash_text.png"), 0, 0));
            widgets.add(new BorderedRectangle(pixel, 20, 20, 600, 30));
            widgets.add(new AnimatedBoxRow(pixel, 20 + 450 + 10, 20, 600 - 450 - 20, 30));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load splash image resources");
        }
    }
}


