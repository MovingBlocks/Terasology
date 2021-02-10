// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.lwjgl;

import com.google.common.collect.Lists;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.lwjgl.LwjglDisplayDevice;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.device.CharKeyboardAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.RawKeyboardAction;

import java.util.Queue;

/**
 * Lwjgl 3's (GLFW) keyboard device representation.
 * Handles keyboard input via GLFW's callbacks.
 */
public class LwjglKeyboardDevice implements KeyboardDevice {

    private static final Logger logger = LoggerFactory.getLogger(LwjglKeyboardDevice.class);
    private static final TIntIntMap GLFW_TO_TERA_MAPPING = new TIntIntHashMap();

    static {
        //TODO: test and cleanup keys
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_NONE, Keyboard.KeyId.NONE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_ESCAPE, Keyboard.KeyId.ESCAPE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_1, Keyboard.KeyId.KEY_1);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_2, Keyboard.KeyId.KEY_2);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_3, Keyboard.KeyId.KEY_3);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_4, Keyboard.KeyId.KEY_4);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_5, Keyboard.KeyId.KEY_5);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_6, Keyboard.KeyId.KEY_6);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_7, Keyboard.KeyId.KEY_7);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_8, Keyboard.KeyId.KEY_8);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_9, Keyboard.KeyId.KEY_9);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_0, Keyboard.KeyId.KEY_0);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_MINUS, Keyboard.KeyId.MINUS);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_EQUAL, Keyboard.KeyId.EQUALS);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_BACKSPACE, Keyboard.KeyId.BACKSPACE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_TAB, Keyboard.KeyId.TAB);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_Q, Keyboard.KeyId.Q);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_W, Keyboard.KeyId.W);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_E, Keyboard.KeyId.E);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_R, Keyboard.KeyId.R);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_T, Keyboard.KeyId.T);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_Y, Keyboard.KeyId.Y);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_U, Keyboard.KeyId.U);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_I, Keyboard.KeyId.I);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_O, Keyboard.KeyId.O);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_P, Keyboard.KeyId.P);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_LEFT_BRACKET, Keyboard.KeyId.LEFT_BRACKET);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_RIGHT_BRACKET, Keyboard.KeyId.RIGHT_BRACKET);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_ENTER, Keyboard.KeyId.ENTER);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_LEFT_CONTROL, Keyboard.KeyId.LEFT_CTRL);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_A, Keyboard.KeyId.A);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_S, Keyboard.KeyId.S);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_D, Keyboard.KeyId.D);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F, Keyboard.KeyId.F);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_G, Keyboard.KeyId.G);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_H, Keyboard.KeyId.H);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_J, Keyboard.KeyId.J);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_K, Keyboard.KeyId.K);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_L, Keyboard.KeyId.L);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_SEMICOLON, Keyboard.KeyId.SEMICOLON);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_APOSTROPHE, Keyboard.KeyId.APOSTROPHE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_GRAVE_ACCENT, Keyboard.KeyId.GRAVE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_LEFT_SHIFT, Keyboard.KeyId.LEFT_SHIFT);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_BACKSLASH, Keyboard.KeyId.BACKSLASH);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_Z, Keyboard.KeyId.Z);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_X, Keyboard.KeyId.X);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_C, Keyboard.KeyId.C);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_V, Keyboard.KeyId.V);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_B, Keyboard.KeyId.B);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_N, Keyboard.KeyId.N);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_M, Keyboard.KeyId.M);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_COMMA, Keyboard.KeyId.COMMA);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_PERIOD, Keyboard.KeyId.PERIOD);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_SLASH, Keyboard.KeyId.SLASH);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_RIGHT_SHIFT, Keyboard.KeyId.RIGHT_SHIFT);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_MULTIPLY, Keyboard.KeyId.NUMPAD_MULTIPLY);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_LEFT_ALT, Keyboard.KeyId.LEFT_ALT);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_SPACE, Keyboard.KeyId.SPACE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_CAPS_LOCK, Keyboard.KeyId.CAPS_LOCK);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F1, Keyboard.KeyId.F1);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F2, Keyboard.KeyId.F2);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F3, Keyboard.KeyId.F3);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F4, Keyboard.KeyId.F4);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F5, Keyboard.KeyId.F5);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F6, Keyboard.KeyId.F6);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F7, Keyboard.KeyId.F7);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F8, Keyboard.KeyId.F8);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F9, Keyboard.KeyId.F9);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F10, Keyboard.KeyId.F10);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_NUM_LOCK, Keyboard.KeyId.NUM_LOCK);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_SCROLL_LOCK, Keyboard.KeyId.SCROLL_LOCK);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_7, Keyboard.KeyId.NUMPAD_7);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_8, Keyboard.KeyId.NUMPAD_8);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_9, Keyboard.KeyId.NUMPAD_9);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_SUBTRACT, Keyboard.KeyId.NUMPAD_MINUS);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_4, Keyboard.KeyId.NUMPAD_4);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_5, Keyboard.KeyId.NUMPAD_5);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_6, Keyboard.KeyId.NUMPAD_6);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_ADD, Keyboard.KeyId.NUMPAD_PLUS);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_1, Keyboard.KeyId.NUMPAD_1);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_2, Keyboard.KeyId.NUMPAD_2);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_3, Keyboard.KeyId.NUMPAD_3);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_0, Keyboard.KeyId.NUMPAD_0);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_DECIMAL, Keyboard.KeyId.NUMPAD_PERIOD);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F11, Keyboard.KeyId.F11);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F12, Keyboard.KeyId.F12);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F13, Keyboard.KeyId.F13);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F14, Keyboard.KeyId.F14);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F15, Keyboard.KeyId.F15);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F16, Keyboard.KeyId.F16);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F17, Keyboard.KeyId.F17);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F18, Keyboard.KeyId.F18);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_KANA, Keyboard.KeyId.KANA);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_F19, Keyboard.KeyId.F19);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_CONVERT, Keyboard.KeyId.CONVERT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_NOCONVERT, Keyboard.KeyId.NOCONVERT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_YEN, Keyboard.KeyId.YEN);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_EQUAL, Keyboard.KeyId.NUMPAD_EQUALS);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_CIRCUMFLEX, Keyboard.KeyId.CIRCUMFLEX);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_AT, Keyboard.KeyId.AT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_COLON, Keyboard.KeyId.COLON);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_UNDERLINE, Keyboard.KeyId.UNDERLINE);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_KANJI, Keyboard.KeyId.KANJI);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_STOP, Keyboard.KeyId.STOP);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_AX, Keyboard.KeyId.AX);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_UNLABELED, Keyboard.KeyId.UNLABELED);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_ENTER, Keyboard.KeyId.NUMPAD_ENTER);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_RIGHT_CONTROL, Keyboard.KeyId.RIGHT_CTRL);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_SECTION, Keyboard.KeyId.SECTION);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_COMMA, Keyboard.KeyId.NUMPAD_COMMA);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_KP_DIVIDE, Keyboard.KeyId.NUMPAD_DIVIDE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_PRINT_SCREEN, Keyboard.KeyId.PRINT_SCREEN);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_RIGHT_ALT, Keyboard.KeyId.RIGHT_ALT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_FUNCTION, Keyboard.KeyId.FUNCTION);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_PAUSE, Keyboard.KeyId.PAUSE);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_HOME, Keyboard.KeyId.HOME);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_UP, Keyboard.KeyId.UP);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_PAGE_UP, Keyboard.KeyId.PAGE_UP);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_LEFT, Keyboard.KeyId.LEFT);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_RIGHT, Keyboard.KeyId.RIGHT);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_END, Keyboard.KeyId.END);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_DOWN, Keyboard.KeyId.DOWN);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_PAGE_DOWN, Keyboard.KeyId.PAGE_DOWN);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_INSERT, Keyboard.KeyId.INSERT);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_DELETE, Keyboard.KeyId.DELETE);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_CLEAR, Keyboard.KeyId.CLEAR);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_LEFT_SUPER, Keyboard.KeyId.LEFT_META);
        GLFW_TO_TERA_MAPPING.put(GLFW.GLFW_KEY_RIGHT_SUPER, Keyboard.KeyId.RIGHT_META);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_APPS, Keyboard.KeyId.APPS);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_POWER, Keyboard.KeyId.POWER);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_SLEEP, Keyboard.KeyId.SLEEP);
    }

    private Queue<RawKeyboardAction> rawKeyQueue = Lists.newLinkedList();
    private Queue<CharKeyboardAction> charQueue = Lists.newLinkedList();
    private TIntSet buttonStates = new TIntHashSet();

    public LwjglKeyboardDevice() {
    }

    public void registerToLwjglWindow(long window) {
        GLFW.glfwSetKeyCallback(window, this::glfwKeyCallback);
        GLFW.glfwSetCharCallback(window, this::glfwCharCallback);
    }

    /**
     * Callback receive char input events from windowing systems. Used for text typing. You cannot receive key,
     * non-printables(mods keys, etc).
     *
     * @param window window's pointer
     * @param chr received char, affected by keyboard layout and modifications.
     */
    private void glfwCharCallback(long window, int chr) {
        charQueue.offer(new CharKeyboardAction((char) chr));
    }

    /**
     * Callback receive key input events. All keys in {@link GLFW}
     *
     * @param window window's pointer
     * @param key one of key listed in {@link GLFW} GLFW_KEY*, also you can see {@link
     *         LwjglKeyboardDevice#GLFW_TO_TERA_MAPPING}
     * @param scancode
     * @param action button state now: {@link GLFW#GLFW_PRESS},{@link GLFW#GLFW_RELEASE} or {@link
     *         GLFW#GLFW_REPEAT}
     * @param mods - modification keys: {@link GLFW#GLFW_MOD_SHIFT} and etc.
     */
    private void glfwKeyCallback(long window, int key, int scancode, int action, int mods) {
        int teraKey = GLFW_TO_TERA_MAPPING.get(key);
        Input input = InputType.KEY.getInput(teraKey);

        ButtonState state;
        if (action == GLFW.GLFW_PRESS) {
            buttonStates.add(teraKey);
            state = ButtonState.DOWN;
        } else if (action == GLFW.GLFW_RELEASE) {
            state = ButtonState.UP;
            buttonStates.remove(teraKey);
        } else /*if (action == GLFW.GLFW_REPEAT)*/ {
            state = ButtonState.REPEAT;
        }

        rawKeyQueue.offer(new RawKeyboardAction(input, state));
    }

    @Override
    public boolean isKeyDown(int key) {
        return buttonStates.contains(key);
    }

    @Override
    public Queue<RawKeyboardAction> getInputQueue() {
        Queue<RawKeyboardAction> rawKeyboardActions = Lists.newLinkedList();
        RawKeyboardAction action;
        while ((action = rawKeyQueue.poll()) != null) {
            rawKeyboardActions.add(action);
        }
        return rawKeyboardActions;
    }

    @Override
    public Queue<CharKeyboardAction> getCharInputQueue() {
        Queue<CharKeyboardAction> charActions = Lists.newLinkedList();
        CharKeyboardAction action;
        while ((action = charQueue.poll()) != null) {
            charActions.add(action);
        }
        return charActions;
    }
}
