// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.splash.graphics.Color;
import org.terasology.engine.splash.graphics.Renderer;
import org.terasology.engine.splash.graphics.Texture;
import org.terasology.engine.splash.graphics.Window;
import org.terasology.engine.splash.widgets.ActivatableImage;
import org.terasology.engine.splash.widgets.AnimatedBoxRow;
import org.terasology.engine.splash.widgets.BorderedRectangle;
import org.terasology.engine.splash.widgets.Image;
import org.terasology.engine.splash.widgets.Widget;
import org.terasology.splash.SplashScreen;
import org.terasology.utilities.LWJGLHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class GLFWSplashScreen implements SplashScreen, Runnable {

    private final CountDownLatch countDownLatch;
    private List<Widget> widgets = new LinkedList<>();
    private Texture pixel;
    private Window window;
    private String message = "Loading...";
    private boolean isClosing;

    public GLFWSplashScreen(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void post(String msg) {
        this.message = msg;
        widgets.stream()
                .filter((w) -> w instanceof ActivatableImage)
                .map((w) -> (ActivatableImage) w)
                .forEach((i) -> i.post(msg));
    }

    @Override
    public void close() {
        if (window != null) {
            isClosing = true;
        }
    }

    @Override
    public void run() {
        LWJGLHelper.initNativeLibs();
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
            widgets.add(new Image("/splash/splash.png", 0, 0));
            widgets.add(new ActivatableImage("/splash/splash_1.png", 0, 0,
                    TerasologyEngineStatus.PREPARING_SUBSYSTEMS));
            widgets.add(new ActivatableImage("/splash/splash_2.png", 150, 0,
                    TerasologyEngineStatus.INITIALIZING_MODULE_MANAGER));
            widgets.add(new ActivatableImage("/splash/splash_3.png", 300, 0,
                    TerasologyEngineStatus.INITIALIZING_ASSET_TYPES));
            widgets.add(new ActivatableImage("/splash/splash_4.png", 450, 0,
                    TerasologyEngineStatus.INITIALIZING_SUBSYSTEMS));
            widgets.add(new ActivatableImage("/splash/splash_5.png", 630, 0,
                    TerasologyEngineStatus.INITIALIZING_ASSET_MANAGEMENT));
            widgets.add(new Image("/splash/splash_text.png", 0, 0));
            widgets.add(new BorderedRectangle(pixel, 20, 20, 600, 30));
            widgets.add(new AnimatedBoxRow(pixel, 20 + 450 + 10, 20, 600 - 450 - 20, 30));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load splash image resources");
        }


        Renderer renderer = new Renderer();
        renderer.init();
        countDownLatch.countDown();
        GL11.glClearColor(0f, 0f, 0f, 0f);
        double last = GLFW.glfwGetTime();
        try {
            while (!isClosing && !window.isClosing()) {
                double dTime = GLFW.glfwGetTime() - last;
                last = GLFW.glfwGetTime();
                renderer.clear();
                widgets.forEach((widget -> widget.update(dTime)));
                widgets.forEach((i) -> i.render(renderer));
                renderer.drawText(message, 30, 25, Color.BLACK);
                window.update();
            }
        } finally {
            widgets.stream()
                    .filter((w) -> w instanceof Image)
                    .map(w -> (Image) w)
                    .forEach(Image::delete);
            pixel.delete();
            renderer.dispose();
            window.destroy();
        }
    }
}


